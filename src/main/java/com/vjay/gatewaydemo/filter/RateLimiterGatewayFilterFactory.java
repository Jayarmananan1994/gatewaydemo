package com.vjay.gatewaydemo.filter;

import com.vjay.gatewaydemo.AppUser;
import com.vjay.gatewaydemo.UserRepository;
import com.vjay.gatewaydemo.UserRequestLimitRepository;
import com.vjay.gatewaydemo.UserRequestRate;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class RateLimiterGatewayFilterFactory extends AbstractGatewayFilterFactory<RateLimiterGatewayFilterFactory.Config> {

    private final UserRequestLimitRepository userRequestLimitRepository;
    private final UserRepository userRepository;

    public RateLimiterGatewayFilterFactory(UserRequestLimitRepository userRequestLimitRepository, UserRepository userRepository) {
        super(Config.class);
        this.userRequestLimitRepository = userRequestLimitRepository;
        this.userRepository = userRepository;
    }

    @Override
    public GatewayFilter apply(Config config) {

        return (exchange, chain) -> {
            try {
                String userName = (String) exchange.getAttributes().get("userName");
                Optional<AppUser> appUserOptional = userRepository.findByUsername(userName);
                AppUser appUser = appUserOptional.orElseThrow();
                UserRequestRate userRequestRate = userRequestLimitRepository.findByUser(appUser).orElseThrow();
                if (userRequestRate.getCount() == 0) {
                    exchange.getResponse().setStatusCode(config.statusCode);
                    return exchange.getResponse().setComplete();
                }

                int newCount = userRequestRate.getCount() - 1;
                userRequestRate.setCount(newCount);
                userRequestLimitRepository.save(userRequestRate);
                exchange.getResponse().getHeaders().add("X-Request-Balance", Integer.toString(newCount));
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
        private HttpStatus statusCode;

    }
}
