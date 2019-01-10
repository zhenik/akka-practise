package ru.zhenik.akka.practise.fail;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.japi.Pair;
import akka.stream.ActorMaterializer;
import akka.stream.DelayOverflowStrategy;
import akka.stream.KillSwitches;
import akka.stream.UniqueKillSwitch;
import akka.stream.javadsl.Keep;
import akka.stream.javadsl.RunnableGraph;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertEquals;

public class ExampleShutdown {
    public static void main(String[] args) throws InterruptedException, ExecutionException, TimeoutException {

        final ActorSystem system = ActorSystem.create();
        final ActorMaterializer mat = ActorMaterializer.create(system);

        final Source<Integer, NotUsed> countingSrc =
                Source.from(new ArrayList<>(Arrays.asList(1, 2, 3, 4)))
                        .delay(Duration.ofSeconds(1), DelayOverflowStrategy.backpressure());
        final Sink<Integer, CompletionStage<Integer>> lastSnk = Sink.last();

        final Pair<UniqueKillSwitch, CompletionStage<Integer>> stream = countingSrc
                .viaMat(KillSwitches.single(), Keep.right())
                .toMat(lastSnk, Keep.both()).run(mat);

        final UniqueKillSwitch killSwitch = stream.first();
        final CompletionStage<Integer> completionStage = stream.second();

        doSomethingElse();
        killSwitch.shutdown();

        final int finalCount = completionStage.toCompletableFuture().get(1, TimeUnit.SECONDS);
        assertEquals(2, finalCount);
    }

    static void doSomethingElse() { System.out.println("doing smth else"); }
}
