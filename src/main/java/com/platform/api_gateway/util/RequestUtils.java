package com.platform.api_gateway.util;

import org.slf4j.MDC;

import java.util.UUID;

/**
 * Utility class for request correlation and logging helpers.
 */
public final class RequestUtils {

    private RequestUtils() {
    }

    /**
     * Initializes a new request ID if not present in MDC.
     *
     * @return the current or newly generated request ID
     */
    public static String ensureRequestId() {
        String requestId = MDC.get("X-Request-Id");
        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString();
            MDC.put("X-Request-Id", requestId);
        }
        return requestId;
    }

    /**
     * Clears request-specific MDC context to prevent memory leaks.
     */
    public static void clear() {
        MDC.remove("X-Request-Id");
        MDC.remove("traceId");
        MDC.remove("spanId");
    }
}
