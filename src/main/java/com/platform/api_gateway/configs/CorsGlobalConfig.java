package com.platform.api_gateway.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Global CORS configuration for the API Gateway.
 * Allows cross-origin requests from all sources.
 */
@Configuration
public class CorsGlobalConfig {

    /**
     * Configures a CorsWebFilter to handle CORS requests globally.
     *
     * @return a CorsWebFilter instance with permissive CORS settings
     */
    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of(
                "http://localhost:*",
                "http://sumar-mas.dynns.com:*",
                "https://sumar-mas.dynns.com"
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "X-User-Id",
                "X-Roles",
                "X-Request-Id"
        ));
        config.setAllowCredentials(true);
        config.setExposedHeaders(List.of(
                "X-Request-Id",
                "fileName",
                "uuid",
                "sha256",
                "mimeType",
                "extension"
        ));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsWebFilter(source);
    }
}
