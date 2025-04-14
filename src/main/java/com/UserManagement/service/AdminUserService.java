package com.UserManagement.service;

import com.UserManagement.model.User;
import com.UserManagement.model.UserTask;
import com.UserManagement.repository.AdminUserRepository;
import io.jsonwebtoken.Jwt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminUserService {

    @Autowired
    private AdminUserRepository userRepository;


    @Autowired
    private PasswordEncoder passwordEncoder;

    public User createUser(User user,Long adminId )
    {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found"));


      //  if (!"ROLE_ADMIN".equalsIgnoreCase(admin.getRole())) {
      //      throw new RuntimeException("Unauthorized: Only admins can create users!");
        //}

        // Encrypt password before saving
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Set createdBy to the admin
        user.setCreatedBy(admin.getId());

        return userRepository.save(user);
    }
    // ✅ Create User (Only Admins can create users)
    public User createAdmin(User user) {
        // Encrypt password before saving
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        return userRepository.save(user);
    }

  /*  // ✅ Get All Users (Only users created by this admin)
    public List<User> getAllUsers(Long adminId) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

//        if (!"ROLE_ADMIN".equalsIgnoreCase(admin.getId())) {
//            throw new RuntimeException("Unauthorized: Only admins can view users!");
//        }

        return userRepository.findByCreatedBy(admin);
    }*/

    // ✅ Get User by ID (Admins only for their users, Users only their own profile)
   /* public User getUserById(Long id, Long requesterId) {
        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new RuntimeException("Requester not found"));

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if ("ROLE_ADMIN".equalsIgnoreCase(requester.getRole())) {
           if (user.getCreatedBy() == null || !user.getCreatedBy().getId().equals(requesterId)) {
                throw new RuntimeException("Unauthorized: Admin can only view their own users!");
            }
        } else if (!requester.getId().equals(id)) {
            throw new RuntimeException("Unauthorized: You can only view your own profile!");
        }

        return user;
    }

    // ✅ Update User (Admins only their users, Users only their own profile)
    public User updateUser(Long id, Long requesterId, User userDetails) {
        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new RuntimeException("Requester not found"));

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if ("ROLE_ADMIN".equalsIgnoreCase(requester.getRole())) {
            if (user.getCreatedBy() == null || !user.getCreatedBy().getId().equals(requesterId)) {
                throw new RuntimeException("Unauthorized: Admin can only update their own users!");
            }
        } else if (!requester.getId().equals(id)) {
            throw new RuntimeException("Unauthorized: You can only update your own profile!");
        }

        user.setUsername(userDetails.getUsername());
        user.setFirstname(userDetails.getFirstname());
        user.setLastname(userDetails.getLastname());
        user.setRole(userDetails.getRole());

        if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userDetails.getPassword()));
        }

        return userRepository.save(user);
    }

    // ✅ Delete User (Only Admins can delete their own users)
    public void deleteUser(Long id, Long adminId) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        if (!"ROLE_ADMIN".equalsIgnoreCase(admin.getRole())) {
            throw new RuntimeException("Unauthorized: Only admins can delete users!");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getCreatedBy() == null || !user.getCreatedBy().getId().equals(adminId)) {
            throw new RuntimeException("Unauthorized: Admin can only delete their own users!");
        }

        userRepository.deleteById(id);
    }*/

    // ✅ Used for login / authentication
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public User findById(Long adminId) {
        return userRepository.findById(adminId).get() ;

    }

    public List findByCreatedBy(long id) {
        return userRepository.findByCreatedBy(id);
    }

    public User updateUser(User user, Long adminId, Long id) {
        User updatedUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!updatedUser.getCreatedBy().equals(adminId)) {
            throw new RuntimeException("This admin does not have access to update this user");
        }

        // Update only the allowed fields
        updatedUser.setFirstname(user.getFirstname());
        updatedUser.setLastname(user.getLastname());
        updatedUser.setUsername(user.getUsername());
        updatedUser.setPassword(user.getPassword());
        updatedUser.setRole(user.getRole());
        // You may keep or skip setting createdBy again

        return userRepository.save(updatedUser);
    }

   /* public void deleteUser(Long adminId, Long id) {
        User deleteUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        if (!deleteUser.getCreatedBy().equals(adminId)) {
            throw new RuntimeException("This admin does not have access to delete this user");
        }

        userRepository.delete(deleteUser);
    }*/

    public void deleteUser(Long adminId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("❌ User not found with ID: " + userId));

        // 🔐 Only the admin who created this user can delete them
        if (!user.getCreatedBy().equals(adminId)) {
            throw new RuntimeException("❌ Unauthorized: You can only delete users you created!");
        }

        userRepository.deleteById(userId);
    }


    public void updatePassword(String username, String newPassword) {
        User user = userRepository.findByUsername(username).get();
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        // Encrypt password before saving
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

}
