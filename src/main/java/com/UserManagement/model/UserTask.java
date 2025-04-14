package com.UserManagement.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class UserTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;
    private String status; // Example: "Pending", "Completed"

    // Many-to-one relationship: each task is assigned to one user.
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)  // Foreign key to User table
    private User user;

    // Optional: Add a method to set user to maintain bidirectional consistency.
    public void setUser(User user) {
        this.user = user;
    }

}
