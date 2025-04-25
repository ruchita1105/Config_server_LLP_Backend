package com.UserManagement.controller;

import com.UserManagement.dto.UserUpdateRequestDTO;
import com.UserManagement.model.User;
import com.UserManagement.security.JwtUtil;
import com.UserManagement.service.AdminUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin
public class AdminUserController {

    @Autowired
    private AdminUserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    // ✅ Get All Users (Only Admins can access this)
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getAllUsers(  @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7); // Removes "Bearer "
        Long adminId = jwtUtil.extractClaim(token, claims -> claims.get("userId", Long.class));
        User admin = userService.findById(adminId);
        return ResponseEntity.ok(userService.findByCreatedBy(adminId));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public User updateUser(@RequestBody UserUpdateRequestDTO userDto,
                           @PathVariable Long id,
                           @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7); // Removes "Bearer "
        Long adminId = jwtUtil.extractClaim(token, claims -> claims.get("userId", Long.class));
        return userService.updateUser(userDto, adminId, id);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteUser(@PathVariable Long id,
                                             @RequestHeader("Authorization") String authHeader) {
        // 1️⃣ Extract token
        String token = authHeader.substring(7); // Remove "Bearer "

        // 2️⃣ Extract adminId from JWT token
        Long adminId = jwtUtil.extractClaim(token, claims -> claims.get("userId", Long.class));

        // 3️⃣ Call service to perform delete logic
        userService.deleteUser(adminId, id);

        // 4️⃣ Return response
        return ResponseEntity.ok("✅ User deleted successfully by Admin ID: " + adminId);
    }


}
