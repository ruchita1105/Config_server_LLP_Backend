package com.UserManagement.repository;

import com.UserManagement.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository  // ✅ Added @Repository annotation
public interface AdminUserRepository extends JpaRepository<User, Long> {

    // ✅ Find a user by their username (used for authentication)
    Optional<User> findByUsername(String username);

    // ✅ Find all users created by a specific admin
    List<User> findByCreatedBy(long id);
    List<User> findByCreatedBy(Long adminId);

}
