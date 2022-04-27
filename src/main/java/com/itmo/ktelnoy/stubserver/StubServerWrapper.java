package com.itmo.ktelnoy.stubserver;

import com.pyruby.stubserver.StubMethod;
import com.pyruby.stubserver.StubServer;
import org.apache.commons.io.FileUtils;
import org.mortbay.jetty.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class StubServerWrapper {

    private final static String APPLICATION_JSON_FORMAT = "application/json";

    private final Logger logger = LoggerFactory.getLogger(StubServerWrapper.class);
    private final StubServer stubServer;

    public StubServerWrapper(int port) {
        this.stubServer = new StubServer(port);
    }

    public void expectedGetJsonResponse(String urlPath, String responseResourceName, int secondsDelay) {
        try {
            String response = FileUtils.fileRead(responseResourceName);

            if (secondsDelay == 0) {
                stubServer.expect(StubMethod.get(urlPath))
                        .thenReturn(Response.SC_OK, APPLICATION_JSON_FORMAT, response);
            } else {
                stubServer.expect(StubMethod.get(urlPath))
                        .delay(secondsDelay, TimeUnit.SECONDS)
                        .thenReturn(Response.SC_OK, APPLICATION_JSON_FORMAT, response);
            }
        } catch (IOException e) {
            logger.error("Error reading json from " + urlPath, e);
        }
    }

    public void expectedGetJsonResponse(String urlPath, String response) {
        expectedGetJsonResponse(urlPath, response, 0);
    }

    public void start() {
        stubServer.start();
    }

    public void stop() {
        stubServer.stop();
    }

}
