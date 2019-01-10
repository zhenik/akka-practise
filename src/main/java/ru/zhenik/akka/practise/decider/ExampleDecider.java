package ru.zhenik.akka.practise.decider;

import akka.Done;
import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.function.Function;
import akka.stream.ActorAttributes;
import akka.stream.ActorMaterializer;
import akka.stream.Supervision;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;

import java.util.Arrays;
import java.util.concurrent.CompletionStage;

public class ExampleDecider {
    public static void main(String[] args) {

        final ActorSystem system = ActorSystem.create();
        final ActorMaterializer mat = ActorMaterializer.create(system);
        final LoggingAdapter logger = Logging.getLogger(system, "example");

        // Deciders
        // deciders can be applied on stream processors independently
        final Function<Throwable, Supervision.Directive> flowDecider = exc -> {
            if (exc instanceof ArithmeticException) return Supervision.resume();else return Supervision.stop(); };
        final Function<Throwable, Supervision.Directive> sourceDecider = exc -> {
            if (exc instanceof NullPointerException) return Supervision.resume();else return Supervision.stop(); };
        final Function<Throwable, Supervision.Directive> streamDecider = exc -> {
            if (exc instanceof Exception) return Supervision.resume(); else return Supervision.stop(); };


        final Source<Integer, NotUsed> source = Source
                .from(Arrays.asList(-1, 0, 1, 2, 3, 7))
                .log("source")
                .map(i -> {
                    if(i==3) {
                        logger.error("source ERROR: {}", i);
                        throw new NullPointerException("sheaaat");
                    }
                    return i;
                })
                .withAttributes(ActorAttributes.withSupervisionStrategy(sourceDecider));

        final Flow<Integer, Integer, NotUsed> divisionFlow = Flow.of(Integer.class)
                .log("divisionFlow")
                .map(i -> 1/i)
                .withAttributes(ActorAttributes.withSupervisionStrategy(flowDecider));

        final Flow<Integer, Integer, NotUsed> boomFlow = Flow.of(Integer.class)
                .log("boomFlow")
                .map(i -> {
                    if (i == 1) {
                        logger.error("boomFlow ERROR: {}", i);
                        throw new Exception("boom");
                    }
                    return i;
                });

        final CompletionStage<Done> completionStage = source
                .via(divisionFlow)
                .via(boomFlow)
                .log("stream-result")
                // commit next line to see difference in behaviour
                .withAttributes(ActorAttributes.withSupervisionStrategy(streamDecider))
                .runWith(Sink.ignore(), mat)
                .handle( (done, throwable) -> {
                    if (throwable!=null){
                        logger.error("Unhandled by decider exception {}", throwable);
                        // if u want to stop actor system on exception in stream
                        system.terminate();
                    }
                    return done;
                });
    }


}
