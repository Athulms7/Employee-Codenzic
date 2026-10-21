package com.codenzic.employeemanagement.controller;

import com.codenzic.employeemanagement.dto.EmployeeRequest;
import com.codenzic.employeemanagement.dto.EmployeeResponse;
import com.codenzic.employeemanagement.entity.EmployeeStatus;
import com.codenzic.employeemanagement.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/employees")
@Tag(name = "Employees", description = "Employee lifecycle operations")
public class EmployeeController {
    private final EmployeeService service;

    public EmployeeController(EmployeeService service) { this.service = service; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create an employee")
    public EmployeeResponse create(@Valid @RequestBody EmployeeRequest request) { return service.create(request); }

    @GetMapping
    @Operation(summary = "List, search, and filter employees",
            description = "Search is case-insensitive. Sort fields: id, firstName, lastName, email, department, jobTitle, salary, status, createdAt, updatedAt. Use sort=lastName,asc or sort=lastName,desc.")
    public Page<EmployeeResponse> findAll(
            @Parameter(description = "Case-insensitive substring matched against first name, last name, email, and department")
            @RequestParam(required = false) String search,
            @Parameter(description = "Filter by employee status")
            @RequestParam(required = false) EmployeeStatus status,
            @ParameterObject @PageableDefault(size = 20, sort = "lastName", direction = Sort.Direction.ASC)
            Pageable pageable) {
        return service.findAll(search, status, pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get an employee by ID")
    public EmployeeResponse findById(@PathVariable Long id) { return service.findById(id); }

    @PutMapping("/{id}")
    @Operation(summary = "Replace an employee's details")
    public EmployeeResponse update(@PathVariable Long id, @Valid @RequestBody EmployeeRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete an employee")
    public void delete(@PathVariable Long id) { service.delete(id); }
}
