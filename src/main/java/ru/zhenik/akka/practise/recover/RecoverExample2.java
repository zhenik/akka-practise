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

public class RecoverExample2 {
    public static void main(String[] args) throws InterruptedException {
        final ActorSystem system = ActorSystem.create();
        final ActorMaterializer mat = ActorMaterializer.create(system);
        final LoggingAdapter logger = Logging.getLogger(system, "example");

        Source<String, NotUsed> planB = Source.from(Arrays.asList("five", "six", "seven", "eight"));

        final CompletionStage completionStage = Source.from(Arrays.asList(0, 1, 2, 3, 4, 5, 6)).map(n -> {
            if (n < 5) return n.toString();
            else throw new RuntimeException("Boom!");
        }).recoverWithRetries(
                1, // max attempts
                new PFBuilder()
                        .match(RuntimeException.class, ex -> planB)
                        .build()
        ).runForeach(System.out::println, mat);

        Thread.sleep(3000);
        System.out.println(completionStage.toCompletableFuture().isDone());
    }
}
