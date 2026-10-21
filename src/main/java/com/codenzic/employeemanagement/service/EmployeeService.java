package com.codenzic.employeemanagement.service;

import com.codenzic.employeemanagement.dto.EmployeeRequest;
import com.codenzic.employeemanagement.dto.EmployeeResponse;
import com.codenzic.employeemanagement.entity.Employee;
import com.codenzic.employeemanagement.entity.EmployeeStatus;
import com.codenzic.employeemanagement.exception.DuplicateEmailException;
import com.codenzic.employeemanagement.exception.ResourceNotFoundException;
import com.codenzic.employeemanagement.mapper.EmployeeMapper;
import com.codenzic.employeemanagement.repository.EmployeeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class EmployeeService {
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
        String normalizedSearch = search == null || search.isBlank() ? null : search.trim();
        return repository.search(normalizedSearch, status, pageable).map(mapper::toResponse);
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
