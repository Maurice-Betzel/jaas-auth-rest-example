/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package com.fleurida.jaas.test;

import org.apache.karaf.jaas.boot.principal.UserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.Principal;
import java.util.Set;

import javax.security.auth.Subject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML,
		MediaType.TEXT_XML })
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML,
		MediaType.TEXT_XML })
@Path("/")
public class EchoServiceImpl {

	protected Logger log = LoggerFactory.getLogger(getClass());

	@GET
	@Path("/jaas/{echotoken}")
	public Response echo(@PathParam("echotoken") String message) {

		Token token = new Token(message);

		// get access to subject in OSGi
		AccessControlContext acc = AccessController.getContext();
		if (acc == null) {
			token.appendError("access control context is null");
		}

		Subject subject = Subject.getSubject(acc);
		if (subject == null) {
			token.appendError("subject is null");
		} else {
			Set<Principal> principals = subject.getPrincipals();
			for (Principal principal : principals) {
				if (principal instanceof UserPrincipal) {
					token.setPrincipal(principal.getName());
				}
			}
		}

		return Response.ok(token).build();

	}

	@GET
	@Path("/jaxrs/{echotoken}")
	public Response echoJAXRSSec(@Context SecurityContext context,
			@PathParam("echotoken") String message) {

		Token token = new Token(message);

		Principal user = context.getUserPrincipal();
		if (user != null) {
			token.setPrincipal(user.getName());
			log.info("User [{}] {} member of users.", user.getName(),
					(context.isUserInRole("users") ? "is" : "is not"));
			log.info("User [{}] {} member of admins.", user.getName(),
					(context.isUserInRole("admins") ? "is" : "is not"));
		} else {
			token.appendError("no user principal in context");
		}
		return Response.ok(token).build();

	}

}