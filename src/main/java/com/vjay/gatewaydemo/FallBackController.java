package com.vjay.gatewaydemo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FallBackController {

    @GetMapping("/demo-app/fallback")
    public String localFallback() {
        return "{\"message\":\"We regret to inform service is currently unavailable. please try again later\"}";
    }
}
