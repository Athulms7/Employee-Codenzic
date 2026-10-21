package com.codenzic.employeemanagement.dto;

import com.codenzic.employeemanagement.entity.EmployeeStatus;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record EmployeeRequest(
        @NotBlank @Size(max = 80) String firstName,
        @NotBlank @Size(max = 80) String lastName,
        @NotBlank @Email @Size(max = 160) String email,
        @NotBlank @Size(max = 100) String department,
        @NotBlank @Size(max = 100) String jobTitle,
        @NotNull @DecimalMin(value = "0.00", inclusive = false) @Digits(integer = 10, fraction = 2) BigDecimal salary,
        @NotNull EmployeeStatus status
) { }
