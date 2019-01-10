package ru.zhenik.akka.practise.streams;

import akka.Done;
import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import akka.stream.javadsl.SubSource;

import java.util.Arrays;
import java.util.concurrent.CompletionStage;

public class SubStreams {
    public static void main(String[] args) {
        final ActorSystem system = ActorSystem.create();
        final ActorMaterializer mat = ActorMaterializer.create(system);
        final LoggingAdapter logger = Logging.getLogger(system, "example");

        final Source<Integer, NotUsed> source = Source.from(Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
        final SubSource<Integer, NotUsed> subSource = source.groupBy(2, elem -> elem % 2);
        final CompletionStage<Done> doneCompletionStage = subSource.mergeSubstreams().runForeach(System.out::println, mat);

    }
}
