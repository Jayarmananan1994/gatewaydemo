package com.vjay.gatewaydemo;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class GateWayDemoApplicationTests {

    @LocalServerPort
    private int port;
    private String baseUri;
    private WebTestClient webClient;
    private static MockWebServer mockWebServer;

    private static String USER_LIST_JSON_RESPONSE = "{\"page\":2,\"per_page\":6,\"total\":12,\"total_pages\":2,\"data\":[{\"id\":7,\"email\":\"michael.lawson@reqres.in\",\"first_name\":\"Michael\",\"last_name\":\"Lawson\",\"avatar\":\"https://reqres.in/img/faces/7-image.jpg\"}," +
            "{\"id\":8,\"email\":\"lindsay.ferguson@reqres.in\",\"first_name\":\"Lindsay\",\"last_name\":\"Ferguson\",\"avatar\":\"https://reqres.in/img/faces/8-image.jpg\"}," +
            "{\"id\":9,\"email\":\"tobias.funke@reqres.in\",\"first_name\":\"Tobias\",\"last_name\":\"Funke\",\"avatar\":\"https://reqres.in/img/faces/9-image.jpg\"}," +
            "{\"id\":10,\"email\":\"byron.fields@reqres.in\",\"first_name\":\"Byron\",\"last_name\":\"Fields\",\"avatar\":\"https://reqres.in/img/faces/10-image.jpg\"}," +
            "{\"id\":11,\"email\":\"george.edwards@reqres.in\",\"first_name\":\"George\",\"last_name\":\"Edwards\",\"avatar\":\"https://reqres.in/img/faces/11-image.jpg\"},{\"id\":12,\"email\":\"rachel.howell@reqres.in\",\"first_name\":\"Rachel\",\"last_name\":\"Howell\",\"avatar\":\"https://reqres.in/img/faces/12-image.jpg\"}]," +
            "\"support\":{\"url\":\"https://reqres.in/#support-heading\",\"text\":\"To keep ReqRes free, contributions towards server costs are appreciated!\"}}";

    private static Dispatcher dispatcher = new Dispatcher() {
        @Override
        public MockResponse dispatch(RecordedRequest request) {
            if ("/api/users".equals(request.getPath())) {
                return new MockResponse().setResponseCode(200)
                        .setHeader("Content-Type", "application/json")
                        .setBody(USER_LIST_JSON_RESPONSE);
            } else if (request.getPath().contains("/api/users?page=2")) {
                return new MockResponse().setBodyDelay(2, TimeUnit.SECONDS).setBody(USER_LIST_JSON_RESPONSE);
            } else if (request.getPath().contains("/api/users?page=10")) {
                return new MockResponse().setBodyDelay(10, TimeUnit.SECONDS).setBody(USER_LIST_JSON_RESPONSE);
            } else if (request.getPath().contains("/api/special-users")) {
                return new MockResponse().setBody(USER_LIST_JSON_RESPONSE);
            }
            return new MockResponse().setResponseCode(HttpStatus.NOT_FOUND.value());
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
                .responseTimeout(Duration.ofSeconds(12))
                .baseUrl(baseUri).build();
    }

    @Test
    void shouldCallValidDownStreamService() {
        webClient.get().uri("/user-service/users")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .json(USER_LIST_JSON_RESPONSE);
    }

    @Test
    void shouldGetResponseIfSubsystemRespondWithinTimeout() {
        webClient.get().uri("/user-service/users?page=2")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .json(USER_LIST_JSON_RESPONSE);
    }

    @Test
    void shouldRespondUnAuthorisedForInvalidAuthorisationHeader() {
        webClient.get().uri("/special-user-service/users")
                .header(HttpHeaders.AUTHORIZATION, "INVALID_HEADER")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void shouldRespondUnAuthorisedRequestIfNoAuthorisationHeader() {
        webClient.get().uri("/special-user-service/users")
                .exchange()
                .expectStatus().isUnauthorized();

    }

    @Test
    void shouldRespondOkRequestWhenValidBearerToken() {
        webClient.get().uri("/special-user-service/users")
                .header(HttpHeaders.AUTHORIZATION, "Bearer "+generateValidToken())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .json(USER_LIST_JSON_RESPONSE);
    }

    private String generateValidToken() {
        return new JwtUtil().generateToken("user1");
    }

}
