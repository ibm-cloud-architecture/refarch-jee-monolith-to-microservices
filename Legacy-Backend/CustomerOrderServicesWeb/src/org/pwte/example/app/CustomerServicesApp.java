package org.pwte.example.app;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;


import org.pwte.example.resources.CustomerOrderResource;


public class CustomerServicesApp extends Application {

	@Override
	public Set<Class<?>> getClasses() {

		Set<Class<?>> classes = new HashSet<Class<?>>();

		
		classes.add(CustomerOrderResource.class);
		
		
		classes.add(org.codehaus.jackson.jaxrs.JacksonJsonProvider.class);
		
		classes.add(com.ibm.websphere.jaxrs.providers.json4j.JSON4JObjectProvider.class);
		classes.add(com.ibm.websphere.jaxrs.providers.json4j.JSON4JArrayProvider.class);
		classes.add(com.ibm.websphere.jaxrs.providers.json4j.JSON4JJAXBProvider.class);

		return classes;

	}
}
