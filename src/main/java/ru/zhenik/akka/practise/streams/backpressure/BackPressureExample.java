package ru.zhenik.akka.practise.streams.backpressure;

import akka.Done;
import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.stream.ActorMaterializer;
import akka.stream.OverflowStrategy;
import akka.stream.ThrottleMode;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Source;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Simple back-pressure example using akka-streams
 *
 * Presentation: https://github.com/sysco-middleware/ideas/files/2759050/reactive-patterns-ougn18.pdf
 * */
public class BackPressureExample {
    public static void main(String[] args) {
        final ActorSystem system = ActorSystem.create();
        final ActorMaterializer mat = ActorMaterializer.create(system);
        final LoggingAdapter logger = Logging.getLogger(system, BackPressureExample.class.getName());

        // try different ranges, buffers and throttling params
        final List<Integer> list = IntStream.rangeClosed(1, 110).boxed().collect(Collectors.toList());
        final Source<Integer, NotUsed> source = Source.from(list);


        final Flow<Integer, Integer, NotUsed> flowSync = Flow.of(Integer.class)
                .map(el -> el * el)
                .buffer(5, OverflowStrategy.backpressure())
                .throttle(2, Duration.ofSeconds(1), 1, ThrottleMode.shaping());

        final Flow<Integer, Integer, NotUsed> flowAsync = Flow.of(Integer.class)
                .mapAsync(4, el -> CompletableFuture.completedFuture(el * el))
                .buffer(2, OverflowStrategy.backpressure());


        source.via(flowSync).runForeach(System.out::println, mat);
//        source.via(flowAsync).runForeach(System.out::println, mat);

    }
}
