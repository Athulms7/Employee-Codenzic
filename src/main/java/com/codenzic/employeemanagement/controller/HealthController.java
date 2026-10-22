package com.codenzic.employeemanagement.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HealthController {
    @GetMapping("/health")
    @Operation(summary = "Check application health")
    @ApiResponse(responseCode = "200", description = "Application is running; response body contains status=UP")
    public Map<String, String> health() {
        return Map.of("status", "UP");
    }
}
