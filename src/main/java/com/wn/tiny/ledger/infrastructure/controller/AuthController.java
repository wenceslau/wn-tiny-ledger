package com.wn.tiny.ledger.infrastructure.controller;

import com.wn.tiny.ledger.infrastructure.config.JWTService;
import com.wn.tiny.ledger.infrastructure.controller.dto.LoginRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final JWTService jwtService;

    public AuthController(JWTService jwtService) {
        this.jwtService = jwtService;
    }

    @PostMapping
    public ResponseEntity<Void> auth(@Valid @RequestBody LoginRequest loginRequest) {

        try {
            // Authentication is not the focus of this example, any user and password will do.

            String jwt = jwtService.generateToken(loginRequest.username(), loginRequest.password());
            ResponseCookie cookie = buildCookie(jwt);

            return ResponseEntity.noContent()
                    .header(HttpHeaders.SET_COOKIE, cookie.toString())
                    .build();

        } catch (Exception e) {
            logger.error("Authentication failed for user '{}': {}", loginRequest.username(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .build();
        }
    }

    private ResponseCookie buildCookie(String jwt) {
        return ResponseCookie.from("access_token", jwt)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(3600000)
                .sameSite("Lax")
                .build();
    }
}
