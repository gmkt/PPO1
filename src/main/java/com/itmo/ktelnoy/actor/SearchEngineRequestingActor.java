package com.itmo.ktelnoy.actor;

import akka.actor.AbstractLoggingActor;
import com.itmo.ktelnoy.ConstantsUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.AbstractMap;

public class SearchEngineRequestingActor extends AbstractLoggingActor {

    private final String engineUrl;
    private final ConstantsUtils.SEARCH_ENGINE searchEngine;

    SearchEngineRequestingActor(String engineUrl, ConstantsUtils.SEARCH_ENGINE searchEngine) {
        this.engineUrl = engineUrl;
        this.searchEngine = searchEngine;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(
                        String.class,
                        query -> {
                            URL url = new URL(engineUrl.concat(query));
                            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                            connection.setRequestMethod("GET");

                            getSender().tell(new AbstractMap.SimpleEntry<>(searchEngine.name(), readFromConnection(connection)), getSelf());
                        })
                .build();
    }

    @Override
    public void postStop() {
        log().info("PostStop");
    }

    private String readFromConnection(HttpURLConnection connection) throws IOException {
        BufferedReader in = new BufferedReader(
                new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuilder result = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            result.append(inputLine);
        }
        in.close();
        return result.toString();
    }
}
