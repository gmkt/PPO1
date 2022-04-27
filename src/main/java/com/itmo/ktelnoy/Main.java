package com.itmo.ktelnoy;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.itmo.ktelnoy.actor.ThreeSearchEnginesMasterActor;
import com.itmo.ktelnoy.stubserver.StubServerWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;

import static akka.pattern.Patterns.ask;

public class Main {

    private final static Logger logger = LoggerFactory.getLogger(StubServerWrapper.class);

    private final static String LOCALHOST_PREFIX = "http://localhost:" + ConstantsUtils.DEFAULT_SERVER_PORT;

    private final static String GOOGLE_PREFIX = LOCALHOST_PREFIX.concat(ConstantsUtils.buildSearchEngineURL(ConstantsUtils.SEARCH_ENGINE.GOOGLE));
    private final static String YANDEX_PREFIX = LOCALHOST_PREFIX.concat(ConstantsUtils.buildSearchEngineURL(ConstantsUtils.SEARCH_ENGINE.YANDEX));
    private final static String BING_PREFIX = LOCALHOST_PREFIX.concat(ConstantsUtils.buildSearchEngineURL(ConstantsUtils.SEARCH_ENGINE.BING));

    public static void main(String[] args) {
        if (args == null || args.length != 1 || args[0] == null) {
            logger.error("Expected single query argument");
            return;
        }

        ActorSystem system = ActorSystem.create("MainSystem");
        ActorRef mainActor = system.actorOf(Props.create(ThreeSearchEnginesMasterActor.class, GOOGLE_PREFIX, YANDEX_PREFIX, BING_PREFIX), "master");

        StubServerWrapper serverWrapper = new StubServerWrapper(ConstantsUtils.DEFAULT_SERVER_PORT);
        try {
            serverWrapper.expectedGetJsonResponse(ConstantsUtils.buildSearchEngineURL(ConstantsUtils.SEARCH_ENGINE.GOOGLE, args[0]),
                    ConstantsUtils.STUB_RESOURCES_PATHS.get(ConstantsUtils.SEARCH_ENGINE.GOOGLE), 100);
            serverWrapper.expectedGetJsonResponse(ConstantsUtils.buildSearchEngineURL(ConstantsUtils.SEARCH_ENGINE.BING, args[0]),
                    ConstantsUtils.STUB_RESOURCES_PATHS.get(ConstantsUtils.SEARCH_ENGINE.BING));
            serverWrapper.expectedGetJsonResponse(ConstantsUtils.buildSearchEngineURL(ConstantsUtils.SEARCH_ENGINE.YANDEX, args[0]),
                    ConstantsUtils.STUB_RESOURCES_PATHS.get(ConstantsUtils.SEARCH_ENGINE.YANDEX));
            serverWrapper.start();

            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> result = (Map<String, Object>) ask(mainActor, args[0], Duration.ofSeconds(3)).toCompletableFuture().join();
                system.stop(mainActor);
                result.forEach((engine, localResult) -> {
                    System.out.println("From: " + engine);
                    System.out.println("Result:\n " + localResult);
                });
            } catch (CompletionException e) {
                logger.error("Exception when computing query result", e);
            } catch (ClassCastException e) {
                logger.error("Invalid format of returned message");
            }
        } finally {
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException ignored) {
            }
            serverWrapper.stop();
        }
    }
}
