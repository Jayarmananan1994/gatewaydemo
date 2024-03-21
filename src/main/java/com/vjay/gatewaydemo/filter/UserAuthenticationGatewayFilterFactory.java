package com.vjay.gatewaydemo.filter;

import com.vjay.gatewaydemo.JwtUtil;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UserAuthenticationGatewayFilterFactory extends AbstractGatewayFilterFactory<UserAuthenticationGatewayFilterFactory.Config> {

    private final JwtUtil jwtUtil;

    public UserAuthenticationGatewayFilterFactory(JwtUtil jwtUtil) {
        super(Config.class);
        this.jwtUtil = jwtUtil;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            try {
                log.info("Incoming request: {} {}", exchange.getRequest().getMethod().name(), exchange.getRequest().getPath());
                String accessToken = exchange.getRequest().getHeaders().get(config.getHeaderName()).stream()
                        .findFirst()
                        .orElseThrow();
                String userName = jwtUtil.getUsernameFromToken(accessToken.substring(7));
                exchange.getAttributes().put("userName", userName);
                return chain.filter(exchange);
            } catch (Exception ex) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }
        };
    }

    @Getter
    @Setter
    @Builder
    public static class Config {
        private String headerName;
    }
}
