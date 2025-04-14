package com.UserManagement.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String password;
    private String role;
    private String firstname;
    private String lastname;

    Long createdBy;

    // One-to-many relationship: one user can have many appointments (tasks).
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore // Prevents infinite recursion when serializing to JSON
    private List<UserTask> appointments;

    // Optional: A method to add tasks for better encapsulation
    public void addAppointment(UserTask task) {
        this.appointments.add(task);
        task.setUser(this);
    }

    // Optional: A method to remove tasks
    public void removeAppointment(UserTask task) {
        this.appointments.remove(task);
        task.setUser(null);
    }
}
