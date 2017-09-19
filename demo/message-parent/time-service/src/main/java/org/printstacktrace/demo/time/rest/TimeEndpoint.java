package org.printstacktrace.demo.time.rest;

import java.time.LocalDateTime;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.wildfly.swarm.health.Health;
import org.wildfly.swarm.health.HealthStatus;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Path("/time")
@Api
public class TimeEndpoint {

	@GET
	@Path("/now")
    @Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Get the current time",
	    notes = "Returns the time as a json LocalDateTime",
	    response = LocalDateTime.class
	)
	public Response now() {
		return Response.ok(LocalDateTime.now()).build();
	}

	@GET
    @Path("/status")
    @Health()
    public HealthStatus status() {
        return HealthStatus.named("status").up();
    }

	
}