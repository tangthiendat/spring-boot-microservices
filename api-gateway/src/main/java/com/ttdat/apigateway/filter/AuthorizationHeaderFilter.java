package com.ttdat.apigateway.filter;

import com.ttdat.apigateway.dto.IntrospectResponse;
import com.ttdat.apigateway.service.IdentityService;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Component
public class AuthorizationHeaderFilter extends AbstractGatewayFilterFactory<AuthorizationHeaderFilter.Config> {

    private final IdentityService identityService;

    public AuthorizationHeaderFilter(IdentityService identityService) {
        super(Config.class);
        this.identityService = identityService;
    }

    public static class Config{
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)){
                return onError(exchange, "No Authorization header", HttpStatus.UNAUTHORIZED);
            }
            final String authHeader = Objects.requireNonNull(request.getHeaders().get(HttpHeaders.AUTHORIZATION)).getFirst();
            if (!authHeader.startsWith("Bearer ")){
                return onError(exchange, "No Bearer token", HttpStatus.UNAUTHORIZED);
            }
            final String token = authHeader.substring(7);
            return identityService.introspect(token)
                    .flatMap(introspectResponse -> {
                        if (!introspectResponse.isValid()){
                            return onError(exchange, "Token is not valid", HttpStatus.UNAUTHORIZED);
                        }
                        return chain.filter(exchange);
                    });
        };
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus){
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        response.bufferFactory().wrap(err.getBytes());
        response.getHeaders().set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        return response.writeWith(
                Mono.just(response.bufferFactory().wrap(err.getBytes())));
    }
}
