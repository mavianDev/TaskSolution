package org.learn.Solution;


import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class CrptApi {
    private static final String URL = "https://ismp.crpt.ru/api/v3/lk/documents/create";
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final ReentrantLock rateLimitLock;
    private final int requestLimit;
    private final Duration interval;
    private Instant nextAllowedTime;

    public CrptApi (TimeUnit timeUnit, int requestLimit) {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
        this.rateLimitLock = new ReentrantLock();
        this.requestLimit = requestLimit;
        this.interval = Duration.ofMillis(timeUnit.toMillis(1));
        this.nextAllowedTime = Instant.now();
    }

    public boolean createDocument(Object document, String signature) {
        rateLimitLock.lock();
        try {
            if (Instant.now().isBefore(nextAllowedTime)) {
                long delay = Duration.between(Instant.now(), nextAllowedTime).toMillis();
                try {
                    TimeUnit.MILLISECONDS.sleep(delay);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            nextAllowedTime = Instant.now().plus(interval);
        } finally {
            rateLimitLock.unlock();
        }

        try {
            String jsonRequest = objectMapper.writeValueAsString(document);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonRequest))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }
}
