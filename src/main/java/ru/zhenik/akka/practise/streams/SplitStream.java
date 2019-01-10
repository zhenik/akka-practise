package ru.zhenik.akka.practise.streams;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import akka.stream.javadsl.SubSource;

import java.util.Arrays;

public class SplitStream {
    public static void main(String[] args) {
        final ActorSystem system = ActorSystem.create();
        final ActorMaterializer mat = ActorMaterializer.create(system);
        final LoggingAdapter logger = Logging.getLogger(system, "example");

        final Source<Integer, NotUsed> source = Source.from(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));

        final SubSource<Integer, NotUsed> even = source.splitWhen(elem -> elem % 2 == 0).log("SplitWhen");
        even.mergeSubstreams().runWith(Sink.ignore(), mat);

        final SubSource<Integer, NotUsed> odd = source.splitAfter(elem -> elem % 2 != 0).log("SplitAfter");
        odd.mergeSubstreams().runWith(Sink.ignore(), mat);



    }
}
