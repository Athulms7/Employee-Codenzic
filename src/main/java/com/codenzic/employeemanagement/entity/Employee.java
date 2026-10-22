package com.codenzic.employeemanagement.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "employees", uniqueConstraints = @UniqueConstraint(name = "uk_employees_email", columnNames = "email"))
@Getter
public class Employee {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", nullable = false, length = 80)
    @Setter
    private String firstName;
    @Column(name = "last_name", nullable = false, length = 80)
    @Setter
    private String lastName;
    @Column(nullable = false, length = 160)
    @Setter
    private String email;
    @Column(nullable = false, length = 100)
    @Setter
    private String department;
    @Column(name = "job_title", nullable = false, length = 100)
    @Setter
    private String jobTitle;
    @Column(nullable = false, precision = 12, scale = 2)
    @Setter
    private BigDecimal salary;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Setter
    private EmployeeStatus status;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public Employee() { }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() { updatedAt = Instant.now(); }
}
