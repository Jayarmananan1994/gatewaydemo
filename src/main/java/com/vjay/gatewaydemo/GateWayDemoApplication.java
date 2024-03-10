package com.vjay.gatewaydemo;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;

import java.time.Duration;

@SpringBootApplication
public class GateWayDemoApplication {

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
        return builder.routes()
                .route("user_route", p -> p.path("/user-service/**")
                        .filters(f -> f.rewritePath("/user-service/(?<suburl>.*)", "/api/${suburl}")
                                .removeResponseHeader("X-Powered-By")
                                .circuitBreaker(c -> c.setName("appCircuitBreaker")
                                        .setFallbackUri("forward:/demo-app/fallback")

                                ))

                        //.uri("https://reqres.in/"))
                        .uri("http://localhost:8081/"))
                .build();
    }


}
