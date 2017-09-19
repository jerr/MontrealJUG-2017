package org.printstacktrace.demo.message;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@ApplicationScoped 
public class PersistenceProducer {


	@PersistenceContext(unitName = "message-service-persistence-unit")
	private EntityManager em;

	@Produces
	public EntityManager produce() {
		return em;
	}
}
