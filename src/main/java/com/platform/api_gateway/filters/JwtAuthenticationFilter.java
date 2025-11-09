package com.platform.api_gateway.filters;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import org.springframework.http.MediaType;

import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Reactive filter that validates JWT tokens and propagates user context headers.
 * Routes under /auth/** or user registration are excluded.
 */
@Component
@Slf4j
@SuppressWarnings("PMD.AtLeastOneConstructor")
public class JwtAuthenticationFilter implements WebFilter {

    /** Secret key used for signing and verifying JWT tokens. */
    @Value("${security.jwt.secret}")
    private String jwtSecret;

    /** Identifier for auth service requests. */
    private static final String AUTH_SERVICE = "auth-service";
    /** Path prefix for authentication-related routes. */
    private static final String AUTH = "/auth/";
    /** Path for user registration endpoint. */
    private static final String REGISTER = "/users/api/v1/users/register";
    /** Path matcher for matching request paths. */
    private static final AntPathMatcher PATH_MATCHER  = new AntPathMatcher();

    /**
     * Filters incoming requests to validate JWT tokens
     * and propagate user context headers.
     *
     * @param exchange the current server exchange
     * @param chain    the web filter chain
     * @return a Mono that indicates when request processing is complete
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        var path = exchange.getRequest().getURI().getPath();

        // Allow preflight requests to pass through
        if (HttpMethod.OPTIONS.equals(exchange.getRequest().getMethod())) {
            return chain.filter(exchange);
        }
        // Public routes (login, register, view campaigns/NGOs)
        if (isPublicRoute(path)) {
            log.debug("Is public route: {}", path);
            return chain.filter(ensureRequestId(exchange));
        }

        // Allow internal service requests to pass through
        if (isInternalRequest(exchange)) {
            log.debug("Is internal request: {}", path);
            return chain.filter(ensureRequestId(exchange));
        }

        var authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        try {
            var token = authHeader.substring(7);
            Claims claims = Jwts.parser()
                    .setSigningKey(jwtSecret.getBytes())
                    .parseClaimsJws(token)
                    .getBody();

            String userId = claims.get("user_id", String.class);
            String roles = claims.get("roles", String.class);

            // ensure request id, and append user headers
            ServerWebExchange updatedExchange = ensureRequestId(exchange)
                    .mutate()
                    .request(exchange.getRequest().mutate()
                            .header("X-User-Id", userId)
                            .header("X-Roles", roles)
                            .build())
                    .build();

            return chain.filter(updatedExchange);

        } catch (io.jsonwebtoken.ExpiredJwtException ex) {
            return writeError(exchange, HttpStatus.UNAUTHORIZED, "Token expired");
        } catch (io.jsonwebtoken.SignatureException | io.jsonwebtoken.MalformedJwtException ex) {
            return writeError(exchange, HttpStatus.UNAUTHORIZED, "Invalid token");
        } catch (Exception ex) {
            return writeError(exchange, HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
    }

    /**
     * Ensures that the request has an X-Request-Id header.
     * If missing, generates a new UUID.
     * @param exchange the current server exchange
     * @return the mutated ServerWebExchange with X-Request-Id header
     */
    private ServerWebExchange ensureRequestId(ServerWebExchange exchange) {
        var headers = exchange.getRequest().getHeaders();
        String requestId = headers.getFirst("X-Request-Id");

        if (requestId == null || requestId.isBlank()) {
            return exchange.mutate()
                    .request(exchange.getRequest().mutate()
                            .header("X-Request-Id", UUID.randomUUID().toString())
                            .build())
                    .build();
        }

        return exchange;
    }

    /**
     * Writes a standardized JSON error response to the client.
     *
     * @param exchange current web exchange
     * @param status   HTTP status code to set
     * @param message  message describing the error
     * @return a Mono signalling when to write is complete
     */
    private Mono<Void> writeError(ServerWebExchange exchange, HttpStatus status, String message) {
        var errorJson = String.format(
                """
                {
                  "status": %d,
                  "error": "%s",
                  "message": "%s",
                  "path": "%s",
                  "timestamp": "%s"
                }
                """,
                status.value(),
                status.getReasonPhrase(),
                message,
                exchange.getRequest().getPath().value(),
                String.valueOf(Timestamp.from(ZonedDateTime.now().toInstant()))
        );

        var response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        var buffer = response.bufferFactory().wrap(errorJson.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    /** Checks if the request is an internal service request.
     *
     * @param exchange the current server exchange
     * @return true if the request is from an internal service, false otherwise
     */
    private boolean isInternalRequest(ServerWebExchange exchange) {
        String internalHeader = exchange.getRequest().getHeaders().getFirst("X-Internal-Request");
        // If not null or blank, it's an internal request
        return Objects.nonNull(internalHeader) && !internalHeader.isBlank();
    }

    /*  Checks if the request path matches any public route patterns.
     *
     * @param path the request path
     * @return true if the path is public, false otherwise
     */
    private boolean isPublicRoute(String path) {
        List<String> publicPatterns = List.of(
                "/auth/**",                                           // login / register auth
                "/users/api/v1/users/register",                       // register
                "/users/api/v1/ngos/all-approved",                    // list approved NGOs
                "/users/api/v1/ngos/{ngoId}",                         // NGO detail
                "/campaigns/api/v1/campaigns/filter",                 // campaign filter
                "/campaigns/api/v1/campaigns/{campaignId}",           // campaign detail
                "/campaigns/api/v1/categories/all",                   // list categories
                "/campaigns/api/v1/comments/{campaignId}",            // campaign comments
                "/campaigns/api/v1/message-campaigns/{campaignId}"    // campaign messages
        );

        return publicPatterns.stream().anyMatch(pattern -> PATH_MATCHER.match(pattern, path));
    }
}
