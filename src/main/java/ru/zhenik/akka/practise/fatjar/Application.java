package ru.zhenik.akka.practise.fatjar;

import akka.actor.ActorSystem;
import akka.stream.ActorMaterializer;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class Application {
    public static void main(String[] args) {

        final Config configDefault = ConfigFactory.load();
        System.out.println(configDefault.getString("some.value"));
        final ActorSystem system = ActorSystem.create();
        final ActorMaterializer mat = ActorMaterializer.create(system);

    }
}
