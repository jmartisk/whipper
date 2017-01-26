package org.whipper.gui.web.rest;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.whipper.WhipperProperties;
import org.whipper.gui.web.WhipperJob;
import org.whipper.gui.web.WhipperJobService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.ResponseHeader;

@Path(WhipperJobManagement.ROOT_RESOURCE_CLASS_PATH)
@Api
public class WhipperJobManagement{

    static final String ROOT_RESOURCE_CLASS_PATH = "job";

    @Inject
    private WhipperJobService runner;
    @Inject
    private org.whipper.gui.web.Context context;

    @Path("start")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiResponses(@ApiResponse(code = 201, message = "Job has been successfully created.", responseHeaders = @ResponseHeader(name = "location", description = "URI to monitoring interface.")))
    @ApiOperation("Creates a new Whipper job.")
    public Response start(@Context UriInfo ui, WhipperProperties props){
        if (props == null) {
            return Response.status(Status.BAD_REQUEST).entity("No properties.").build();
        }
        String id = runner.startNewJob(props);
        return Response.created(ui.getBaseUriBuilder().path(ROOT_RESOURCE_CLASS_PATH).path(id).build()).build();
    }

    @Path("{id:[a-zA-Z0-9]{" + WhipperJobService.ID_LENGTH + "}}/stop")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiResponses({
        @ApiResponse(code = 204, message = "If job has been successfully canceled or has already finished."),
        @ApiResponse(code = 404, message = "If job with specified ID does not exist.") })
    @ApiOperation("Stops execution of Whipper job.")
    public Response stop(@ApiParam(value = "Job ID.", name = "id", required = true) @PathParam("id") String id){
        WhipperJob wj = runner.getJob(id);
        if (wj == null) {
            return Response.status(Status.NOT_FOUND).build();
        }
        wj.stop();
        return Response.noContent().build();
    }

    @Path("{id:[a-zA-Z0-9]{" + WhipperJobService.ID_LENGTH + "}}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiResponses({
        @ApiResponse(code = 200, message = "Result successfully returned."),
        @ApiResponse(code = 404, message = "If job with specified ID does not exist.") })
    @ApiOperation("Retrieves result of Whipper job.")
    public Response results(@ApiParam(value = "Job ID.", name = "id", required = true) @PathParam("id") String id){
        WhipperJob wj = runner.getJob(id);
        if (wj == null) {
            return Response.status(Status.NOT_FOUND).build();
        }
        return Response.ok(wj.resultToJson()).build();
    }

    @Path("{id:[a-zA-Z0-9]{" + WhipperJobService.ID_LENGTH + "}}/full")
    @GET
    @Produces("application/zip")
    public Response resultsAsZip(@PathParam("id") String id){
        WhipperJob wj = runner.getJob(id);
        if (wj == null) {
            return Response.status(Status.NOT_FOUND).build();
        }
        if (wj.finished()) {
            return Response.ok(wj.resultToZip(context)).build();
        }
        return Response.noContent().build();
    }

    @Path("{id:[a-zA-Z0-9]{" + WhipperJobService.ID_LENGTH + "}}")
    @DELETE
    @ApiResponses(@ApiResponse(code = 204, message = "Result successfully deleted."))
    @ApiOperation("Deletes results of Whipper job.")
    public Response delete(@ApiParam(value = "Job ID.", name = "id", required = true) @PathParam("id") String id){
        runner.deleteJob(id);
        return Response.noContent().build();
    }
}
