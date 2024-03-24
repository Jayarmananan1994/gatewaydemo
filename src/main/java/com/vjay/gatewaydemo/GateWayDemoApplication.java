package com.vjay.gatewaydemo;

import com.vjay.gatewaydemo.filter.RateLimiterGatewayFilterFactory;
import com.vjay.gatewaydemo.filter.UserAuthenticationGatewayFilterFactory;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

import java.time.Duration;

@SpringBootApplication
@RequiredArgsConstructor
public class GateWayDemoApplication {

    private final UserAuthenticationGatewayFilterFactory userAuthenticationGatewayFilterFactory;
    private final RateLimiterGatewayFilterFactory rateLimiterGatewayFilterFactory;

    @Value("${downstream.baseurl:http://localhost:8081}")
    private String baseUrl;

    public static void main(String[] args) {
        SpringApplication.run(GateWayDemoApplication.class, args);
    }

    //@Bean
    public Customizer<ReactiveResilience4JCircuitBreakerFactory> defaultCustomizer() {
        return factory -> factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
                .circuitBreakerConfig(CircuitBreakerConfig.custom()
                        .failureRateThreshold(50)
                        .slidingWindowSize(20)
                        .minimumNumberOfCalls(10)
                        .waitDurationInOpenState(Duration.ofSeconds(10))
                        .build())
                .timeLimiterConfig(TimeLimiterConfig.custom().timeoutDuration(Duration.ofSeconds(5)).build()).build());
    }

    //@Bean
    public RouteLocator myRoutes(RouteLocatorBuilder builder) {
        UserAuthenticationGatewayFilterFactory.Config config = UserAuthenticationGatewayFilterFactory.Config.builder()
                .headerName(HttpHeaders.AUTHORIZATION)
                .build();
        RateLimiterGatewayFilterFactory.Config rateLimiterConfig = RateLimiterGatewayFilterFactory.Config.builder()
                .statusCode(HttpStatus.TOO_MANY_REQUESTS)
                .build();
        GatewayFilter authenticationGatewayFilter = userAuthenticationGatewayFilterFactory.apply(config);
        GatewayFilter rateLimiterGatewayFilter = rateLimiterGatewayFilterFactory.apply(rateLimiterConfig);
        return builder.routes()
                .route("user_route", p -> p.path("/user-service/**")
                        .filters(f -> f.rewritePath("/user-service/(?<suburl>.*)", "/api/${suburl}")
                                .removeResponseHeader("X-Powered-By")
                                .circuitBreaker(c -> c.setName("appCircuitBreaker").setFallbackUri("forward:/demo-app/fallback")))
                        .uri(baseUrl))
                .route("protected-user-route", p -> p.path("/special-user-service/users")
                        .filters(f -> f.rewritePath("/special-user-service/users", "/api/special-users")
                                .filter(authenticationGatewayFilter)
                                .filter(rateLimiterGatewayFilter))
                        .uri(baseUrl)
                )
                .build();
    }

}
