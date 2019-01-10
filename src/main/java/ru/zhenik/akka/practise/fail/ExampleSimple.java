package ru.zhenik.akka.practise.fail;

import akka.Done;
import akka.actor.ActorSystem;

import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.function.Function;
import akka.stream.ActorAttributes;
import akka.stream.ActorMaterializer;
import akka.stream.Supervision;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;

import java.util.Arrays;
import java.util.concurrent.CompletionStage;

public class ExampleSimple {
    public static void main(String[] args) {

        final ActorSystem system = ActorSystem.create();
        final ActorMaterializer mat = ActorMaterializer.create(system);
        final LoggingAdapter logger = Logging.getLogger(system, "example");

        final Function<Throwable, Supervision.Directive> decider = exc -> {
            if (exc instanceof ArithmeticException) {
                logger.info("Handled exception, keep streaming: {}", exc);
                return Supervision.resume();
            }
            else {
                logger.error("Unhandled exception: {}", exc);
                return Supervision.stop();
            }
        };

        final CompletionStage<Done> completionStage = Source.from(Arrays.asList(-1, 0, 1))
                .log("Processing")
                .map(x -> 1 / x) //throwing ArithmeticException: / by zero
                .log("error logging")
                .withAttributes(ActorAttributes.withSupervisionStrategy(decider))
                .runWith(Sink.ignore(), mat);

        //todo: how to shutdown
//        completionStage.handle((k,v)-> {
//           if (v instanceof ArithmeticException) system.terminate();
//        });
    }
}
