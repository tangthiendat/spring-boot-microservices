package com.ttdat.photoappuserservice.controller;

import com.ttdat.photoappuserservice.dto.*;
import com.ttdat.photoappuserservice.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final Environment env;
    private final UserService userService;

    @GetMapping("/status/check")
    public String status() {
        return "Working on port " + env.getProperty("local.server.port");
    }

    @PostMapping
    public ResponseEntity<UserDTO> createUser(@Valid @RequestBody UserDTO userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userService.createUser(userDetails));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request) {
        return ResponseEntity.ok(userService.authenticate(request));
    }

    @PostMapping("/introspect")
    public ResponseEntity<IntrospectResponse> introspect(@RequestBody IntrospectRequest request) {
        return ResponseEntity.ok(userService.introspect(request));
    }

    @PostMapping("/validate")
    public ResponseEntity<Boolean> validate(HttpServletRequest request) {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        final String accessToken;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            accessToken = authHeader.substring(7);
            return ResponseEntity.ok(userService.isTokenValid(accessToken));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(false);
        }
    }

}
