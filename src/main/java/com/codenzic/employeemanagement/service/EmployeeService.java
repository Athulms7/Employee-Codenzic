package com.codenzic.employeemanagement.service;

import com.codenzic.employeemanagement.dto.EmployeeRequest;
import com.codenzic.employeemanagement.dto.EmployeeResponse;
import com.codenzic.employeemanagement.entity.Employee;
import com.codenzic.employeemanagement.entity.EmployeeStatus;
import com.codenzic.employeemanagement.exception.DuplicateEmailException;
import com.codenzic.employeemanagement.exception.InvalidRequestException;
import com.codenzic.employeemanagement.exception.ResourceNotFoundException;
import com.codenzic.employeemanagement.mapper.EmployeeMapper;
import com.codenzic.employeemanagement.repository.EmployeeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class EmployeeService {
    private static final int MAX_PAGE_SIZE = 100;
    private static final List<String> SORTABLE_FIELDS = List.of(
            "id", "firstName", "lastName", "email", "department", "jobTitle",
            "salary", "status", "createdAt", "updatedAt");

    private final EmployeeRepository repository;
    private final EmployeeMapper mapper;

    public EmployeeService(EmployeeRepository repository, EmployeeMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public EmployeeResponse create(EmployeeRequest request) {
        if (repository.existsByEmailIgnoreCase(request.email().trim())) throw new DuplicateEmailException(request.email());
        return mapper.toResponse(repository.save(mapper.toEntity(request)));
    }

    @Transactional(readOnly = true)
    public Page<EmployeeResponse> findAll(String search, EmployeeStatus status, Pageable pageable) {
        validatePageable(pageable);
        String normalizedSearch = search == null || search.isBlank() ? "" : search.trim();
        return repository.search(normalizedSearch, status, pageable).map(mapper::toResponse);
    }

    private void validatePageable(Pageable pageable) {
        if (pageable.getPageSize() > MAX_PAGE_SIZE) {
            throw new InvalidRequestException("Page size must not exceed " + MAX_PAGE_SIZE);
        }

        Set<String> unsupportedFields = pageable.getSort().stream()
                .map(Sort.Order::getProperty)
                .filter(property -> !SORTABLE_FIELDS.contains(property))
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (!unsupportedFields.isEmpty()) {
            throw new InvalidRequestException("Unsupported sort field(s): " + String.join(", ", unsupportedFields)
                    + ". Allowed fields: " + String.join(", ", SORTABLE_FIELDS));
        }
    }

    @Transactional(readOnly = true)
    public EmployeeResponse findById(Long id) { return mapper.toResponse(getEmployee(id)); }

    public EmployeeResponse update(Long id, EmployeeRequest request) {
        Employee employee = getEmployee(id);
        if (repository.existsByEmailIgnoreCaseAndIdNot(request.email().trim(), id)) throw new DuplicateEmailException(request.email());
        mapper.updateEntity(employee, request);
        return mapper.toResponse(repository.save(employee));
    }

    public void delete(Long id) { repository.delete(getEmployee(id)); }

    private Employee getEmployee(Long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Employee with id " + id + " was not found"));
    }
}
