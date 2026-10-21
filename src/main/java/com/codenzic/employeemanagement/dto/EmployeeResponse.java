package com.codenzic.employeemanagement.dto;

import com.codenzic.employeemanagement.entity.EmployeeStatus;
import java.math.BigDecimal;
import java.time.Instant;

public record EmployeeResponse(Long id, String firstName, String lastName, String email,
                               String department, String jobTitle, BigDecimal salary,
                               EmployeeStatus status, Instant createdAt, Instant updatedAt) { }
