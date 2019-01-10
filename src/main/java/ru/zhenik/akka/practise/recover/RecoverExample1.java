package ru.zhenik.akka.practise.recover;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.pf.PFBuilder;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Source;

import java.util.Arrays;
import java.util.concurrent.CompletionStage;

public class RecoverExample1 {
    public static void main(String[] args) throws InterruptedException {
        final ActorSystem system = ActorSystem.create();
        final ActorMaterializer mat = ActorMaterializer.create(system);
        final LoggingAdapter logger = Logging.getLogger(system, "example");

        final CompletionStage completionStage = Source.from(Arrays.asList(0, 1, 2, 3, 4, 5, 99, 4, 3))
                .map(n -> {
                    if (n < 5) return n.toString();
                    else throw new RuntimeException("Boom!");
                })
                .recover(new PFBuilder()
                        .match(RuntimeException.class, ex -> "stream truncated")
                        .build())
                .runForeach(System.out::println, mat);
        Thread.sleep(3000);
        System.out.println(completionStage.toCompletableFuture().isDone());
    }
}
