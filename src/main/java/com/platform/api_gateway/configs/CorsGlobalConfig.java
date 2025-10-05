package com.platform.api_gateway.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

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
        config.addAllowedOriginPattern("*"); // allow all origins
        config.addAllowedMethod("*");        // allow all HTTP methods
        config.addAllowedHeader("*");        // allow all headers
        config.setAllowCredentials(true);    // allow credentials

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsWebFilter(source);
    }
}
