package com.vjay.gatewaydemo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class GatewaydemoApplication {

    @Value("${downstream.baseurl}")
    private String downStreamBaseUrl;

    public static void main(String[] args) {
        SpringApplication.run(GatewaydemoApplication.class, args);
    }

    @Bean
    public RouteLocator myRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("basic_get", p -> p
                        .path("/get")
                        .filters(f -> f.addRequestHeader("uuid", "1234"))
                        .uri(downStreamBaseUrl))
                .route("path_example", p -> p.path("/foo/{segment}")
                        .filters(f -> f.rewritePath("/foo/(?<segment>.*)", "/segment/${segment}"))
                        .uri(downStreamBaseUrl))
                .route("astricks_example", p -> p.path("/par/**")
                        .filters(f -> f.rewritePath("/par/(?<suburl>.*)", "/api/par/${suburl}"))
                        .uri(downStreamBaseUrl))
                .build();
    }
}
