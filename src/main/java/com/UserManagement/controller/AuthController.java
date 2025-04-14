package com.UserManagement.controller;

import com.UserManagement.model.User;
import com.UserManagement.security.JwtUtil;
import com.UserManagement.service.EmailService;
import com.UserManagement.service.AdminUserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private AdminUserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private EmailService emailService;
    private Map<String, String> otpStorage = new ConcurrentHashMap<>();
    private Random random = new Random();

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User loginRequest, HttpServletRequest request) {
        try {
            // Authenticate credentials
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            // Fetch full user from DB to get role
            User resUser = userService.findByUsername(userDetails.getUsername());

            // Generate JWT tokens with dynamic role
            String accessToken = jwtUtil.generateAccessToken(resUser.getUsername(),resUser.getId(), resUser.getRole());
            String refreshToken = jwtUtil.generateRefreshToken(resUser.getUsername(),resUser.getId(), resUser.getRole());

            // Create HTTP session
            HttpSession session = request.getSession(true);
            String sessionId = session.getId();

            // Optional: Send email notification
            emailService.sendEmail(resUser.getUsername(), "Login Notification", "You have successfully logged in.");

            // Build response
            Map<String, Object> response = new HashMap<>();
            response.put("token", accessToken);
            response.put("refreshToken", refreshToken);
            response.put("sessionId", sessionId);
            response.put("userId", resUser.getId());
            response.put("role", resUser.getRole());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Invalid username or password");
            return ResponseEntity.status(401).body(errorResponse);
        }
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> requestBody) {
        String refreshToken = requestBody.get("refreshToken");

        if (refreshToken == null || refreshToken.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Refresh token is required"));
        }

        try {
            String username = jwtUtil.extractUsername(refreshToken);

            if (jwtUtil.validateToken(refreshToken, username)) {
                // 👇 IMPORTANT: Also extract role from token
                String role = jwtUtil.extractClaim(refreshToken, claims -> claims.get("role", String.class));
                Long userId = jwtUtil.extractClaim(refreshToken, claims -> claims.get("userId", Long.class));

                String newAccessToken = jwtUtil.generateAccessToken(username, userId,role);
                return ResponseEntity.ok(Map.of("token", newAccessToken));
            } else {
                return ResponseEntity.status(401).body(Map.of("error", "Invalid refresh token"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid or expired refresh token"));
        }
    }

    @PostMapping("/register")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> register(@RequestBody User user, @RequestParam Long adminId) {
        try {
            User savedUser = userService.createUser(user, adminId);
            return ResponseEntity.ok(savedUser);
        } catch (Exception e) {
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        }
    }
    @PostMapping("/registerAdmin")
    public ResponseEntity<?> registerAdmin(@RequestBody User user) {
        try {
            User savedUser = userService.createAdmin(user);
            return ResponseEntity.ok(savedUser);
        } catch (Exception e) {
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        }
    }
    @PostMapping("/registerUser")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> registerUser(@RequestBody User user,   @RequestHeader("Authorization") String authHeader) {

        try {
            String token = authHeader.substring(7); // Removes "Bearer "
            Long adminId = jwtUtil.extractClaim(token, claims -> claims.get("userId", Long.class));
            User admin = userService.findById(adminId);
            user.setCreatedBy(admin.getId());


            User savedUser = userService.createAdmin (user);
            emailService.sendEmail(savedUser.getUsername(), "Registration Notification", "You have successfully Registered .");
            return ResponseEntity.ok(savedUser);
        } catch (Exception e) {
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> sendOtp(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        User user = userService.findByUsername(email);

        if (user == null) {
            return ResponseEntity.status(404).body(Map.of("error", "User not found"));
        }

        // Generate a 6-digit OTP
        String otp = String.format("%06d", random.nextInt(999999));

        // Store OTP
        otpStorage.put(email, otp);

        // Send OTP email
        String subject = "Password Reset OTP";
        String message = "Your OTP for password reset is: " + otp;
        emailService.sendEmail(email, subject, message);

        return ResponseEntity.ok(Map.of("message", "OTP sent to email"));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String otp = request.get("otp");

        String storedOtp = otpStorage.get(email);
        if (storedOtp != null && storedOtp.equals(otp)) {
            return ResponseEntity.ok(Map.of("message", "OTP verified"));
        } else {
            return ResponseEntity.status(400).body(Map.of("error", "Invalid OTP"));
        }
    }
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String newPassword = request.get("newPassword");

        // Ensure OTP was previously verified
        if (!otpStorage.containsKey(email)) {
            return ResponseEntity.status(400).body(Map.of("error", "OTP verification required"));
        }

        try {
            userService.updatePassword(email, newPassword);
            otpStorage.remove(email); // Clear OTP after password reset
            return ResponseEntity.ok(Map.of("message", "Password reset successful"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }



}
