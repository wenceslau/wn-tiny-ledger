package com.wn.tiny.ledger.infrastructure.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

@Service
public class JWTService {

    private final SecretKey secretKey;

    public JWTService(@Value("${jwt.secret}") String secretKey) {
        this.secretKey = Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    public String generateToken(String userId, String jwtId) {
        return Jwts.builder()
                .subject(userId)
                .id(jwtId)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600000L))
                .signWith(this.secretKey)
                .compact();
    }

    public String getTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }
        return Arrays.stream(request.getCookies())
                .filter(cookie -> "access_token".equals(cookie.getName())) // Use the injected cookie name
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }

    public Authentication getAuthentication(String token) {
        Jws<Claims> claims = Jwts.parser().verifyWith(this.secretKey).build().parseSignedClaims(token);

        if (claims == null) {
            throw new RuntimeException("Invalid JWT token");
        }
        String userId = claims.getPayload().getSubject();
        String JwtId = claims.getPayload().getId();

        if (userId == null || JwtId == null) {
            throw new RuntimeException("User id or JWT id is null");
        }

        var userAuthToken = new UsernamePasswordAuthenticationToken(
                userId,
                null,
                Collections.emptyList()
        );

        userAuthToken.setDetails(JwtId);

        return userAuthToken;
    }

}
