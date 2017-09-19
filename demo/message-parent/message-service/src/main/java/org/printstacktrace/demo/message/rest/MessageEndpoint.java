package org.printstacktrace.demo.message.rest;

import java.nio.charset.Charset;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.OptimisticLockException;
import javax.persistence.TypedQuery;
import javax.transaction.UserTransaction;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;

import org.printstacktrace.demo.message.model.Message;
import org.printstacktrace.demo.message.service.TimeService;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.swagger.annotations.Api;
import rx.Observable;

@Path("/messages")
@Api
public class MessageEndpoint {
	
	@Inject
	private EntityManager em;
    
	@Resource
    private UserTransaction userTransaction;

    
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String get() {
        return String.format("Message Service");
    }

	@POST
	@Consumes("application/json")
	public Response create(Message entity) {
		em.persist(entity);
		return Response.created(
				UriBuilder.fromResource(MessageEndpoint.class)
						.path(String.valueOf(entity.getId())).build()).build();
	}

	@DELETE
	@Path("/{id:[0-9][0-9]*}")
	public Response deleteById(@PathParam("id") Long id) {
		Message entity = em.find(Message.class, id);
		if (entity == null) {
			return Response.status(Status.NOT_FOUND).build();
		}
		em.remove(entity);
		return Response.noContent().build();
	}

	@GET
	@Path("/{id:[0-9][0-9]*}")
	@Produces("application/json")
	public Response findById(@PathParam("id") Long id) {
		TypedQuery<Message> findByIdQuery = em
				.createQuery(
						"SELECT DISTINCT m FROM Message m WHERE m.id = :entityId ORDER BY m.id",
						Message.class);
		findByIdQuery.setParameter("entityId", id);
		Message entity;
		try {
			entity = findByIdQuery.getSingleResult();
		} catch (NoResultException nre) {
			entity = null;
		}
		if (entity == null) {
			return Response.status(Status.NOT_FOUND).build();
		}
		return Response.ok(entity).build();
	}

	@Path("/list")
	@GET
	@Produces("application/json")
	public List<Message> listAll(@QueryParam("start") Integer startPosition,
			@QueryParam("max") Integer maxResult, @QueryParam("last") @DefaultValue("true") boolean lastFirst) {
		System.out.println(" em:" + em );
		TypedQuery<Message> findAllQuery = em
				.createQuery("SELECT DISTINCT m FROM Message m ORDER BY m.id " + (lastFirst?"DESC":"ASC"),
						Message.class);
		if (startPosition != null) {
			findAllQuery.setFirstResult(startPosition);
		}
		if (maxResult != null) {
			findAllQuery.setMaxResults(maxResult);
		}
		final List<Message> results = findAllQuery.getResultList();
		return results;
	}

	@PUT
	@Path("/{id:[0-9][0-9]*}")
	@Consumes("application/json")
	public Response update(@PathParam("id") Long id, Message entity) {
		if (entity == null) {
			return Response.status(Status.BAD_REQUEST).build();
		}
		if (id == null) {
			return Response.status(Status.BAD_REQUEST).build();
		}
		if (!id.equals(entity.getId())) {
			return Response.status(Status.CONFLICT).entity(entity).build();
		}
		if (em.find(Message.class, id) == null) {
			return Response.status(Status.NOT_FOUND).build();
		}
		try {
			entity = em.merge(entity);
		} catch (OptimisticLockException e) {
			return Response.status(Response.Status.CONFLICT)
					.entity(e.getEntity()).build();
		}

		return Response.noContent().build();
	}
	
    @GET
    @Path("/tic")
    @Produces(MediaType.APPLICATION_JSON)
    public void tic(@Suspended final AsyncResponse asyncResponse) {
        Observable<ByteBuf> obs = TimeService.INSTANCE.now().observe();
        obs.subscribe(
                (result) -> {
                    try {
                    	String time = result.toString(Charset.defaultCharset());
                    	try {	
	                        ObjectMapper mapper = new ObjectMapper();
	                        ObjectReader reader = mapper.reader();
	                        JsonFactory factory = new JsonFactory();
	                        JsonParser parser = factory.createParser(new ByteBufInputStream(result));
	                        time = reader.readValue(parser, Map.class).toString().replace('{',' ').replace('}',' ');
                    	}catch (JsonProcessingException e) {
                    		try {
                    			time = (new Date(Long.valueOf(time))).toString();
                    		}catch(NumberFormatException ex) {
                    			
                    		}
						}
                        Message msg = new Message();
                        msg.setDate(Timestamp.from(Instant.now()));
                        msg.setMessage("Tic at " + time);

                        userTransaction.begin();
                        em.persist(msg);
                        userTransaction.commit();
                        asyncResponse.resume(msg);
                    } catch (Exception e) {
                        System.err.println("ERROR: " + e.getLocalizedMessage());
                        asyncResponse.resume(e);
                    }
                },
                (err) -> {
                    System.err.println("ERROR: " + err.getLocalizedMessage());
                    asyncResponse.resume(err);
                });
    }

}
