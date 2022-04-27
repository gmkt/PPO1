package com.itmo.ktelnoy;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

public class ConstantsUtils {

    public enum SEARCH_ENGINE {
        GOOGLE, BING, YANDEX
    }

    public final static Map<SEARCH_ENGINE, String> STUB_URL_PATHS = ImmutableMap.<SEARCH_ENGINE, String>builder()
            .put(SEARCH_ENGINE.GOOGLE, "/google?q=")
            .put(SEARCH_ENGINE.BING, "/bing?q=")
            .put(SEARCH_ENGINE.YANDEX, "/yandex?q=")
            .build();

    public final static Map<SEARCH_ENGINE, String> STUB_RESOURCES_PATHS = ImmutableMap.<SEARCH_ENGINE, String>builder()
            .put(SEARCH_ENGINE.GOOGLE, "src\\main\\resources\\googleResponse.json")
            .put(SEARCH_ENGINE.BING, "src\\main\\resources\\bingResponse.json")
            .put(SEARCH_ENGINE.YANDEX, "src\\main\\resources\\yandexResponse.json")
            .build();

    public final static int DEFAULT_SERVER_PORT = 228;

    public static String buildSearchEngineURL(SEARCH_ENGINE searchEngine, String query) {
        return STUB_URL_PATHS.get(searchEngine).concat(query);
    }

    public static String buildSearchEngineURL(SEARCH_ENGINE searchEngine) {
        return STUB_URL_PATHS.get(searchEngine);
    }

}
