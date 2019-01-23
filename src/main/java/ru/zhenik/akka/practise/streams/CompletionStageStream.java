package ru.zhenik.akka.practise.streams;

import akka.Done;
import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CompletionStageStream {
    public static void main(String[] args) throws InterruptedException {
        final ActorSystem system = ActorSystem.create();
        final ActorMaterializer mat = ActorMaterializer.create(system);
        final LoggingAdapter logger = Logging.getLogger(system, "example");

        final List<Integer> list = IntStream.rangeClosed(1, 110).boxed().collect(Collectors.toList());
        final Source<Integer, NotUsed> source = Source.from(list);

        final CompletionStage<Done> doneCompletionStage = source.runWith(Sink.ignore(), mat);

        System.out.println("\nIs done: "+doneCompletionStage.toCompletableFuture().isDone() + "\n");


        doneCompletionStage.whenComplete((done,throwable)-> {
            System.out.println("Done: "+done);
            System.out.println("Throwable: "+throwable);
        });

        doneCompletionStage.thenRun(() -> System.out.println("\nNew runnable executed"));

        Thread.sleep(500);
        System.out.println("\nIs done: "+doneCompletionStage.toCompletableFuture().isDone());


    }
}
