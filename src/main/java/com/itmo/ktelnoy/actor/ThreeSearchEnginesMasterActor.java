package com.itmo.ktelnoy.actor;

import akka.actor.AbstractLoggingActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.pattern.AskTimeoutException;
import com.itmo.ktelnoy.ConstantsUtils;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static akka.pattern.Patterns.ask;

public class ThreeSearchEnginesMasterActor extends AbstractLoggingActor {

    private final static int ACTOR_TIMEOUT = 2;

    private final Map<Object, Object> result = new HashMap<>();

    private final String firstPrefix;
    private final String secondPrefix;
    private final String thirdPrefix;

    public ThreeSearchEnginesMasterActor(String firstPrefix, String secondPrefix, String thirdPrefix) {
        this.firstPrefix = firstPrefix;
        this.secondPrefix = secondPrefix;
        this.thirdPrefix = thirdPrefix;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(
                        String.class,
                        query -> {
                            ActorRef actor1 = getContext().actorOf(Props.create(SearchEngineRequestingActor.class,
                                    firstPrefix, ConstantsUtils.SEARCH_ENGINE.GOOGLE));
                            ActorRef actor2 = getContext().actorOf(Props.create(SearchEngineRequestingActor.class,
                                    secondPrefix, ConstantsUtils.SEARCH_ENGINE.YANDEX));
                            ActorRef actor3 = getContext().actorOf(Props.create(SearchEngineRequestingActor.class,
                                    thirdPrefix, ConstantsUtils.SEARCH_ENGINE.BING));

                            CompletableFuture<Void> result1 = getAskedFuture(actor1, query);
                            CompletableFuture<Void> result2 = getAskedFuture(actor2, query);
                            CompletableFuture<Void> result3 = getAskedFuture(actor3, query);
                            CompletableFuture.allOf(result1, result2, result3).join();

                            getSender().tell(result, getSelf());
                        })
                .build();
    }

    @Override
    public void postStop() {
        log().info("Master stopped");
    }

    private CompletableFuture<Void> getAskedFuture(ActorRef actor, Object query) {
        return ask(actor, query, Duration.ofSeconds(ACTOR_TIMEOUT)).exceptionally(e -> {
            if (!(e instanceof AskTimeoutException)) {
                log().error("Unexpected exception: ", e);
            }
            return null;
        }).toCompletableFuture().thenAccept(localQuery -> {
            if (localQuery instanceof Map.Entry){
                result.put(((Map.Entry<Object, Object>) localQuery).getKey(), ((Map.Entry<Object, Object>) localQuery).getValue());
            }
        });
    }
}
