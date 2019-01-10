package ru.zhenik.akka.practise.decider;

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
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ExampleDeciderRestart {
    public static void main(String[] args) throws InterruptedException, ExecutionException, TimeoutException {
        final ActorSystem system = ActorSystem.create();
        final ActorMaterializer mat = ActorMaterializer.create(system);
        final LoggingAdapter logger = Logging.getLogger(system, "example");

        final Function<Throwable, Supervision.Directive> decider = exc -> {
            if (exc instanceof IllegalArgumentException)
                return Supervision.restart();
            else
                return Supervision.stop();
        };
        final Flow<Integer, Integer, NotUsed> flow = Flow.of(Integer.class)
                .scan(0, (acc, elem) -> {
                    if (elem < 0) throw new IllegalArgumentException("negative not allowed");
                    else return acc + elem;
                })
                .map(i -> {
                    System.out.println(i);
                    return i;
                })
                .withAttributes(ActorAttributes.withSupervisionStrategy(decider));
        final Source<Integer, NotUsed> source = Source.from(Arrays.asList(1, 3, -1, 5, 7))
                .via(flow);

        final CompletionStage<List<Integer>> result = source.grouped(1000)
                .runWith(Sink.<List<Integer>>head(), mat);

        final List<Integer> integers = result.toCompletableFuture().get(1, TimeUnit.SECONDS);
        System.out.println(integers);

        // the negative element cause the scan stage to be restarted,
        // i.e. start from 0 again
        // result here will be a Future completed with Success(List(0, 1, 4, 0, 5, 12))
    }
}
