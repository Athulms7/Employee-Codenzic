package com.codenzic.employeemanagement.repository;

import com.codenzic.employeemanagement.entity.Employee;
import com.codenzic.employeemanagement.entity.EmployeeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository 
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    @Query("""
            select e from Employee e
            where (:status is null or e.status = :status)
              and (:search is null or lower(e.firstName) like lower(concat('%', :search, '%'))
                   or lower(e.lastName) like lower(concat('%', :search, '%'))
                   or lower(e.email) like lower(concat('%', :search, '%'))
                   or lower(e.department) like lower(concat('%', :search, '%')))
            """)
    Page<Employee> search(@Param("search") String search, @Param("status") EmployeeStatus status, Pageable pageable);
}
