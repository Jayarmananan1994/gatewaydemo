package com.vjay.gatewaydemo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@AllArgsConstructor
public class UserAuthenticationController {
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @PostMapping("/authenticate")
    public String authenticate(@RequestBody UserCredentials credentials) {
        if (validateUserCredential(credentials)) {
            return jwtUtil.generateToken(credentials.username);
        } else {
            throw new RuntimeException("Invalid username or password");
        }
    }

    private boolean validateUserCredential(UserCredentials credentials) {
        Optional<AppUser> userOptional = userRepository.findByUsername(credentials.username);
        return userOptional.map(user -> credentials.password.equals(user.getUsrPassword()) && credentials.username.equals(user.getUsername()))
                .orElse(false);
    }

    @Getter
    @AllArgsConstructor
    static class UserCredentials {
        private String username;
        private String password;
    }
}
