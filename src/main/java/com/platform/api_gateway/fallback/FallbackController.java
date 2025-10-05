package com.platform.api_gateway.fallback;

import com.platform.api_gateway.dtos.ErrorApi;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Timestamp;
import java.time.ZonedDateTime;

/**
 * Unified fallback controller for all microservices.
 */
@RestController
@RequestMapping("/fallback")
public class FallbackController {
    /**
     * Handles fallback responses when a downstream service is unavailable.
     *
     * @param service the name of the failed service
     * @return standardized error response
     */
    @RequestMapping(value = "/{service}", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<ErrorApi> fallback(@PathVariable String service) {
        ErrorApi error = ErrorApi.builder()
                .timestamp(String.valueOf(Timestamp.from(ZonedDateTime.now().toInstant())))
                .status(HttpStatus.SERVICE_UNAVAILABLE.value())
                .error("Service Unavailable")
                .message("Service '" + service + "' is temporarily unavailable. Please try again later.")
                .build();

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
    }
}
