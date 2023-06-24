package com.vjay.gatewaydemo;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.io.IOException;
import java.time.Duration;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class GatewaydemoApplicationTests {
    @LocalServerPort
    private int port = 0;
    private String baseUri;
    private WebTestClient webClient;

    private static MockWebServer mockWebServer;

    private static Dispatcher dispatcher = new Dispatcher() {
        @Override
        public MockResponse dispatch(RecordedRequest request) {
            switch (request.getPath()) {
                case "/segment/mysegment":
                    return new MockResponse().setResponseCode(200)
                            .setHeader("Content-Type", "application/json")
                            .setBody("{\"segment\": \"mysegment\"}");

                case "/api/par/route/data":
                    return new MockResponse().setResponseCode(200)
                            .setHeader("Content-Type", "application/json")
                            .setBody("{\"route\": \"data\"}");

                case "/api/par/flag/myflag":
                    return new MockResponse().setResponseCode(200)
                            .setHeader("Content-Type", "application/json")
                            .setBody("{\"flag\": \"myflag\"}");
                default:
                    return new MockResponse().setResponseCode(HttpStatus.NOT_FOUND.value());
            }
        }
    };

    @BeforeAll
    static void setupServer() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.setDispatcher(dispatcher);
        mockWebServer.start(9090);
    }

    @AfterAll
    static void tearDownServer() throws IOException {
        mockWebServer.shutdown();
    }

    @BeforeEach
    public void setup() {
        baseUri = "http://localhost:" + port;
        this.webClient = WebTestClient.bindToServer()
                .responseTimeout(Duration.ofSeconds(10))
                .baseUrl(baseUri).build();
    }

    @Test
    void shouldCallValidDownStreamService() {
        webClient.get().uri("/foo/mysegment")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .json("{\"segment\": \"mysegment\"}");
    }

    @ParameterizedTest
    @CsvSource(value = {"/par/route/data,{\"route\": \"data\"}", "/par/flag/myflag,{\"flag\": \"myflag\"}"})
    void shouldCallValidDownStreamServiceWhileUsingAstric(String inputUrl, String expectedOutput) {
        webClient.get().uri(inputUrl)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .json(expectedOutput);
    }


}
