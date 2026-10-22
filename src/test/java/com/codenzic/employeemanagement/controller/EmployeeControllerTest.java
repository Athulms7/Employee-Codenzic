package com.codenzic.employeemanagement.controller;

import com.codenzic.employeemanagement.dto.EmployeeRequest;
import com.codenzic.employeemanagement.dto.EmployeeResponse;
import com.codenzic.employeemanagement.entity.EmployeeStatus;
import com.codenzic.employeemanagement.exception.GlobalExceptionHandler;
import com.codenzic.employeemanagement.service.EmployeeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EmployeeController.class)
@Import(GlobalExceptionHandler.class)
class EmployeeControllerTest {
    @Autowired
    MockMvc mvc;

    @MockBean
    EmployeeService service;

    private final EmployeeResponse employee = new EmployeeResponse(
            7L, "Asha", "Rao", "asha@example.com", "Engineering", "Developer",
            new BigDecimal("75000.00"), EmployeeStatus.ACTIVE,
            Instant.parse("2026-10-21T12:00:00Z"), Instant.parse("2026-10-21T12:00:00Z"));

    private static final String VALID_REQUEST = """
            {
              "firstName": "Asha",
              "lastName": "Rao",
              "email": "asha@example.com",
              "department": "Engineering",
              "jobTitle": "Developer",
              "salary": 75000.00,
              "status": "ACTIVE"
            }
            """;

    @Test
    void createReturnsCreatedEmployee() throws Exception {
        when(service.create(any(EmployeeRequest.class))).thenReturn(employee);

        mvc.perform(post("/api/v1/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REQUEST))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.email").value("asha@example.com"));

        verify(service).create(any(EmployeeRequest.class));
    }

    @Test
    void listReturnsRequestedPageAndPassesFiltersAndSortToService() throws Exception {
        var pageable = PageRequest.of(2, 2, Sort.by(Sort.Direction.DESC, "lastName"));
        when(service.findAll("engineering", EmployeeStatus.ACTIVE, pageable))
                .thenReturn(new PageImpl<>(List.of(employee), pageable, 5));

        mvc.perform(get("/api/v1/employees")
                        .param("search", "engineering")
                        .param("status", "ACTIVE")
                        .param("page", "2")
                        .param("size", "2")
                        .param("sort", "lastName,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(7))
                .andExpect(jsonPath("$.number").value(2))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalElements").value(5))
                .andExpect(jsonPath("$.totalPages").value(3));

        verify(service).findAll("engineering", EmployeeStatus.ACTIVE, pageable);
    }

    @Test
    void listUsesDefaultPageSizeAndSort() throws Exception {
        var pageable = PageRequest.of(0, 20, Sort.by("lastName"));
        when(service.findAll(null, null, pageable))
                .thenReturn(new PageImpl<>(List.of(employee), pageable, 1));

        mvc.perform(get("/api/v1/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(service).findAll(null, null, pageable);
    }

    @Test
    void findByIdReturnsEmployee() throws Exception {
        when(service.findById(7L)).thenReturn(employee);

        mvc.perform(get("/api/v1/employees/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.firstName").value("Asha"));

        verify(service).findById(7L);
    }

    @Test
    void updateReturnsUpdatedEmployee() throws Exception {
        when(service.update(eq(7L), any(EmployeeRequest.class))).thenReturn(employee);

        mvc.perform(put("/api/v1/employees/7")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REQUEST))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.email").value("asha@example.com"));

        verify(service).update(eq(7L), any(EmployeeRequest.class));
    }

    @Test
    void deleteReturnsNoContent() throws Exception {
        mvc.perform(delete("/api/v1/employees/7"))
                .andExpect(status().isNoContent());

        verify(service).delete(7L);
    }

    @Test
    void invalidCreateReturnsConsistentValidationBody() throws Exception {
        mvc.perform(post("/api/v1/employees").contentType(MediaType.APPLICATION_JSON).content("""
                {"firstName":"","lastName":"","email":"invalid","department":"","jobTitle":"","salary":-1,"status":null}
                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Request validation failed"))
                .andExpect(jsonPath("$.validationErrors.email").exists());
        verifyNoInteractions(service);
    }

    @Test
    void databaseQueryFailureReturnsInternalServerError() throws Exception {
        var pageable = PageRequest.of(0, 20, Sort.by("lastName"));
        when(service.findAll(null, null, pageable))
                .thenThrow(new InvalidDataAccessResourceUsageException("Invalid database query"));

        mvc.perform(get("/api/v1/employees"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Database operation failed"));
    }

    @Test
    void databaseConnectivityFailureReturnsServiceUnavailable() throws Exception {
        var pageable = PageRequest.of(0, 20, Sort.by("lastName"));
        when(service.findAll(null, null, pageable))
                .thenThrow(new DataAccessResourceFailureException("Database is unreachable"));

        mvc.perform(get("/api/v1/employees"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.message").value("Database is temporarily unavailable"));
    }
}
