package com.platform.api_gateway.configs;


import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationPredicate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * Global tracing configuration that filters out noisy and internal endpoints
 * such as Eureka heartbeats and health checks, preventing them from appearing
 * in Zipkin traces.
 * This configuration is compatible with Spring Boot 3.x and Micrometer Tracing.
 */
@Configuration
public class TracingConfig {
    /** URI path for the actuator health endpoint. */
    private static final String ACTUATOR_HEALTH_URI = "/actuator/health";
    /** URI path for the actuator Prometheus endpoint. */
    private static final String ACTUATOR_PROMETHEUS_URI = "/actuator/prometheus";

    /**
     * Defines an ObservationPredicate bean that filters out noisy endpoints
     * from being traced.
     *
     * @return an ObservationPredicate that ignores specific URIs
     */
    @Bean
    public ObservationPredicate ignoreNoisyEndpoints() {
        return new ObservationPredicate() {
            @Override
            public boolean test(String observationName, Observation.Context context) {

                // Micrometer identifies server requests with this observation name
                if (observationName != null && observationName.contains("http.server.requests")) {

                    // Extract the URI stored as a low-cardinality key
                    String uri = context.getLowCardinalityKeyValue("uri") != null
                            ? context.getLowCardinalityKeyValue("uri").getValue()
                            : null;

                    if (uri != null) {

                        // 🚫 Ignore Eureka traffic entirely
                        if (uri.startsWith("/eureka")) {
                            return false;
                        }

                        // 🚫 Ignore health checks
                        if (ACTUATOR_HEALTH_URI.equals(uri) || uri.startsWith("/actuator/health/")) {
                            return false;
                        }

                        // 🚫 Ignore Prometheus scraping (optional)
                        if (ACTUATOR_PROMETHEUS_URI.equals(uri)) {
                            return false;
                        }
                    }
                }

                return true; // Allow everything else
            }
        };
    }
}
