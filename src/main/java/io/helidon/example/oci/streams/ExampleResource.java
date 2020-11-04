package io.helidon.example.oci.streams;

import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import io.helidon.common.reactive.BufferedEmittingPublisher;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import org.reactivestreams.FlowAdapters;
import org.reactivestreams.Publisher;

@Path("/example")
@ApplicationScoped
public class ExampleResource {

    private final SubmissionPublisher<String> broadCastPublisher = new SubmissionPublisher<>();
    private final BufferedEmittingPublisher<Message<String>> emitter = BufferedEmittingPublisher.create();

    @Incoming("from-stream")
    public void eachMessageFromStream(String payload) {
        // Broadcast every message received from configured OCI Stream
        // to subscribed SSE clients
        broadCastPublisher.submit(payload);
    }

    @Outgoing("to-stream")
    public Publisher<Message<String>> registerEmitter() {
        return FlowAdapters.toPublisher(emitter);
    }

    @Path("/send/{msg}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public void getSend(@PathParam("msg") String msg) {
        // Send message to the emitter registered as a publisher of "to-stream" channel
        emitter.emit(Message.of(msg));
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
