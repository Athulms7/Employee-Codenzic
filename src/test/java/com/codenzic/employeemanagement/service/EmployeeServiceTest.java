package com.codenzic.employeemanagement.service;

import com.codenzic.employeemanagement.dto.EmployeeRequest;
import com.codenzic.employeemanagement.entity.Employee;
import com.codenzic.employeemanagement.entity.EmployeeStatus;
import com.codenzic.employeemanagement.exception.DuplicateEmailException;
import com.codenzic.employeemanagement.exception.ResourceNotFoundException;
import com.codenzic.employeemanagement.mapper.EmployeeMapper;
import com.codenzic.employeemanagement.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import java.math.BigDecimal;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {
    @Mock EmployeeRepository repository;
    @InjectMocks EmployeeService service;
    EmployeeMapper mapper;
    EmployeeRequest request;

    @BeforeEach
    void setUp() {
        mapper = new EmployeeMapper();
        service = new EmployeeService(repository, mapper);
        request = new EmployeeRequest("Asha", "Rao", "asha@example.com", "Engineering", "Developer",
                new BigDecimal("75000.00"), EmployeeStatus.ACTIVE);
    }

    @Test
    void createsEmployeeWithNormalizedEmail() {
        when(repository.existsByEmailIgnoreCase(request.email())).thenReturn(false);
        when(repository.save(any(Employee.class))).thenAnswer(invocation -> {
            Employee employee = invocation.getArgument(0);
            return employee;
        });
        var response = service.create(request);
        assertEquals("asha@example.com", response.email());
        assertEquals("Asha", response.firstName());
    }

    @Test
    void rejectsDuplicateEmail() {
        when(repository.existsByEmailIgnoreCase(request.email())).thenReturn(true);
        assertThrows(DuplicateEmailException.class, () -> service.create(request));
        verify(repository, never()).save(any());
    }

    @Test
    void reportsMissingEmployee() {
        when(repository.findById(42L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.findById(42L));
    }

    @Test
    void usesEmptySearchForUnfilteredEmployeeList() {
        var pageable = PageRequest.of(0, 20, Sort.by("lastName"));
        when(repository.search("", null, pageable)).thenReturn(Page.empty(pageable));

        var response = service.findAll(null, null, pageable);

        assertTrue(response.isEmpty());
        verify(repository).search("", null, pageable);
    }
}
