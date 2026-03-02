package org.acme;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSConsumer;
import jakarta.jms.JMSContext;
import jakarta.jms.JMSException;
import jakarta.jms.Queue;
import jakarta.jms.TextMessage;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Path("/hello")
@ApplicationScoped
public class GreetingResource {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        return "Hello from Quarkus REST";
    }

    @Inject
    ConnectionFactory connectionFactory;

    @Inject
    ObjectMapper objectMapper;

    @RestClient
    TestRestClient testRestClient;

    @GET
    @Path("/send")
    public String send() throws JsonProcessingException {
        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("message", "Sent to queue.1");
        objectNode.put("timestamp", System.currentTimeMillis());
        String jsonString = objectMapper.writeValueAsString(objectNode);
        
        try (JMSContext context = connectionFactory.createContext()) {
            Queue queue = context.createQueue("DEV.QUEUE.1");
            TextMessage textMessage = context.createTextMessage(jsonString);
            context.createProducer().send(queue, textMessage);
        }

        return jsonString;
    }

    @GET
    @Path("/receive")
    public JsonNode receive() throws JsonProcessingException {
        String jsonString = null;

        try (JMSContext context = connectionFactory.createContext()) {
            Queue queue = context.createQueue("DEV.QUEUE.1");
            JMSConsumer consumer = context.createConsumer(queue);
            jsonString = consumer.receiveBody(String.class, 2000L);
        }

        return objectMapper.readTree(jsonString);
    }

    @GET
    @Path("/send2")
    public String send2() throws JsonProcessingException {
        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("message", "Sent to queue.2");
        objectNode.put("timestamp", System.currentTimeMillis());
        String jsonString = objectMapper.writeValueAsString(objectNode);

        try (JMSContext context = connectionFactory.createContext()) {
            Queue queue = context.createQueue("DEV.QUEUE.2");
            TextMessage textMessage = context.createTextMessage(jsonString);
            context.createProducer().send(queue, textMessage);
        }

        return jsonString;
    }

    @GET
    @Path("/call-rest")
    public String callRest() {
        return testRestClient.callHello();
    }

}
