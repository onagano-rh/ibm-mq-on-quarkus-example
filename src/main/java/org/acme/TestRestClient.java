package org.acme;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

@RegisterRestClient(configKey = "test-rest-client")
public interface TestRestClient {

    @GET
    @Path("/hello")
    String callHello();
}