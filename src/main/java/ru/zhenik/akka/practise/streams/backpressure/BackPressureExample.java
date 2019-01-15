package ru.zhenik.akka.practise.streams.backpressure;

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
        final List<Integer> list = IntStream.rangeClosed(1, 11).boxed().collect(Collectors.toList());
        final Source<Integer, NotUsed> source = Source.from(list)
                .buffer(5, OverflowStrategy.backpressure())
                .throttle(1, Duration.ofSeconds(1), 1, ThrottleMode.shaping());

        final Flow<Integer, Integer, NotUsed> flow = Flow.of(Integer.class).map(el -> el * el);

        source.via(flow).runForeach(System.out::println, mat);

    }
}
