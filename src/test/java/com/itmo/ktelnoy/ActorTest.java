package com.itmo.ktelnoy;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.google.common.collect.ImmutableSet;
import com.itmo.ktelnoy.actor.ThreeSearchEnginesMasterActor;
import com.itmo.ktelnoy.stubserver.StubServerWrapper;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;

import static akka.pattern.Patterns.ask;

public class ActorTest {

    private final static Logger logger = LoggerFactory.getLogger(StubServerWrapper.class);

    private final static String LOCALHOST_PREFIX = "http://localhost:" + ConstantsUtils.DEFAULT_SERVER_PORT;

    private final static String GOOGLE_PREFIX = LOCALHOST_PREFIX.concat(ConstantsUtils.buildSearchEngineURL(ConstantsUtils.SEARCH_ENGINE.GOOGLE));
    private final static String YANDEX_PREFIX = LOCALHOST_PREFIX.concat(ConstantsUtils.buildSearchEngineURL(ConstantsUtils.SEARCH_ENGINE.YANDEX));
    private final static String BING_PREFIX = LOCALHOST_PREFIX.concat(ConstantsUtils.buildSearchEngineURL(ConstantsUtils.SEARCH_ENGINE.BING));
    private final static String QUERY = "query";

    @Test
    public void testSearchEngineRequestingActorOneNotRespondingInTime() {
        ActorSystem system = ActorSystem.create("MainSystem");
        ActorRef mainActor = system.actorOf(Props.create(ThreeSearchEnginesMasterActor.class, GOOGLE_PREFIX, YANDEX_PREFIX, BING_PREFIX), "master");

        StubServerWrapper serverWrapper = new StubServerWrapper(ConstantsUtils.DEFAULT_SERVER_PORT);
        try {
            serverWrapper.expectedGetJsonResponse(ConstantsUtils.buildSearchEngineURL(ConstantsUtils.SEARCH_ENGINE.GOOGLE, QUERY),
                    ConstantsUtils.STUB_RESOURCES_PATHS.get(ConstantsUtils.SEARCH_ENGINE.GOOGLE), 100);
            serverWrapper.expectedGetJsonResponse(ConstantsUtils.buildSearchEngineURL(ConstantsUtils.SEARCH_ENGINE.BING, QUERY),
                    ConstantsUtils.STUB_RESOURCES_PATHS.get(ConstantsUtils.SEARCH_ENGINE.BING));
            serverWrapper.expectedGetJsonResponse(ConstantsUtils.buildSearchEngineURL(ConstantsUtils.SEARCH_ENGINE.YANDEX, QUERY),
                    ConstantsUtils.STUB_RESOURCES_PATHS.get(ConstantsUtils.SEARCH_ENGINE.YANDEX));
            serverWrapper.start();

            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> result = (Map<String, Object>) ask(mainActor, QUERY, Duration.ofSeconds(3)).toCompletableFuture().join();
                system.stop(mainActor);

                Assert.assertEquals(2, result.size());
                Assert.assertEquals(ImmutableSet.of(ConstantsUtils.SEARCH_ENGINE.YANDEX.name(), ConstantsUtils.SEARCH_ENGINE.BING.name()), result.keySet());
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

    @Test
    public void testSearchEngineRequestingActorAllRespondingInTime() {
        ActorSystem system = ActorSystem.create("MainSystem");
        ActorRef mainActor = system.actorOf(Props.create(ThreeSearchEnginesMasterActor.class, GOOGLE_PREFIX, YANDEX_PREFIX, BING_PREFIX), "master");

        StubServerWrapper serverWrapper = new StubServerWrapper(ConstantsUtils.DEFAULT_SERVER_PORT);
        try {
            serverWrapper.expectedGetJsonResponse(ConstantsUtils.buildSearchEngineURL(ConstantsUtils.SEARCH_ENGINE.GOOGLE, QUERY),
                    ConstantsUtils.STUB_RESOURCES_PATHS.get(ConstantsUtils.SEARCH_ENGINE.GOOGLE));
            serverWrapper.expectedGetJsonResponse(ConstantsUtils.buildSearchEngineURL(ConstantsUtils.SEARCH_ENGINE.BING, QUERY),
                    ConstantsUtils.STUB_RESOURCES_PATHS.get(ConstantsUtils.SEARCH_ENGINE.BING));
            serverWrapper.expectedGetJsonResponse(ConstantsUtils.buildSearchEngineURL(ConstantsUtils.SEARCH_ENGINE.YANDEX, QUERY),
                    ConstantsUtils.STUB_RESOURCES_PATHS.get(ConstantsUtils.SEARCH_ENGINE.YANDEX));
            serverWrapper.start();

            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> result = (Map<String, Object>) ask(mainActor, QUERY, Duration.ofSeconds(3)).toCompletableFuture().join();
                system.stop(mainActor);

                Assert.assertEquals(3, result.size());
                Assert.assertEquals(ImmutableSet.of(ConstantsUtils.SEARCH_ENGINE.GOOGLE.name(),
                        ConstantsUtils.SEARCH_ENGINE.YANDEX.name(), ConstantsUtils.SEARCH_ENGINE.BING.name()), result.keySet());
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
