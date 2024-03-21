package com.vjay.gatewaydemo;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@Slf4j
public class JwtUtil {
    public static final String BASE_64_ENCODED_SECRET_KEY = "3WrtzuOyCX3WrtzuOyCX3WrtzuOyCX3WrtzuOyCX3WrtzuOyCX3WrtzuOyCX";

    public String generateToken(String username) {
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);
        long expiryMillis = nowMillis + (1000 * 60 * 60);
        Date expiryDate = new Date(expiryMillis);
        String token = Jwts.builder()
                .subject(username)
                .issuedAt(now)
                .issuer("jkay")
                .expiration(expiryDate)
                .signWith(SignatureAlgorithm.HS256, BASE_64_ENCODED_SECRET_KEY)
                .compact();
        return token;
    }

    public String getUsernameFromToken(String jwtToken) {

        Jws<Claims> claims = Jwts.parser()
                .setSigningKey(BASE_64_ENCODED_SECRET_KEY)
                .build()
                .parseClaimsJws(jwtToken);
        return claims.getPayload().getSubject();
    }
}
