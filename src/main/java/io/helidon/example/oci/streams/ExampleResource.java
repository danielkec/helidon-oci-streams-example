package io.helidon.example.oci.streams;

import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;

import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

import org.eclipse.microprofile.reactive.messaging.Incoming;

@Path("/ui/example")
@ApplicationScoped
public class ExampleResource {

    private final SubmissionPublisher<String> broadCastPublisher = new SubmissionPublisher<>();

    @Inject
    @Channel("to-stream")
    Emitter<String> emitter;

    @Incoming("from-stream")
    public void eachMessageFromStream(String payload) {
        // Broadcast every message received from configured OCI Stream
        // to subscribed SSE clients
        broadCastPublisher.submit(payload);
    }


    @Path("/send/{msg}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public void getSend(@PathParam("msg") String msg) {
        // Send message to the emitter registered as a publisher of "to-stream" channel
        emitter.send(msg);
    }

    @GET
    @Path("sse")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public void listenToEvents(@Context Flow.Subscriber<String> sink) {
        // SubmissionPublisher supports multiple subscriptions
        // Every SSE client subscribe for broadcasted messages
        broadCastPublisher.subscribe(sink);
    }
}
