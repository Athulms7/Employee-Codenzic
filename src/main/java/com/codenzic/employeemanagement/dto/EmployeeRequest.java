package com.codenzic.employeemanagement.dto;

import com.codenzic.employeemanagement.entity.EmployeeStatus;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record EmployeeRequest(
        @NotBlank(message = "must not be blank") @Size(max = 80, message = "must be at most 80 characters")
        String firstName,
        @NotBlank(message = "must not be blank") @Size(max = 80, message = "must be at most 80 characters")
        String lastName,
        @NotBlank(message = "must not be blank") @Email(message = "must be a valid email address")
        @Size(max = 160, message = "must be at most 160 characters") String email,
        @NotBlank(message = "must not be blank") @Size(max = 100, message = "must be at most 100 characters")
        String department,
        @NotBlank(message = "must not be blank") @Size(max = 100, message = "must be at most 100 characters")
        String jobTitle,
        @NotNull(message = "is required")
        @DecimalMin(value = "0.00", inclusive = false, message = "must be greater than 0")
        @Digits(integer = 10, fraction = 2, message = "must have at most 10 integer digits and 2 decimal places")
        BigDecimal salary,
        @NotNull(message = "is required") EmployeeStatus status
) { }
