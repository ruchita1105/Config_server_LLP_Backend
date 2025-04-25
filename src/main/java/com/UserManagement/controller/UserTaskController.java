package com.UserManagement.controller;

import com.UserManagement.model.UserTask;
import com.UserManagement.security.JwtUtil;
import com.UserManagement.service.UserTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

 @RestController
@RequestMapping("/api/tasks")
public class UserTaskController {

    @Autowired

    private UserTaskService userTaskService;

    @Autowired
    private JwtUtil jwtUtil;

    // ✅ Add Task (Users can only add tasks to their own account)
    @PostMapping()
    public ResponseEntity<UserTask> addUserTask(@RequestBody UserTask userTask, @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7); // Removes "Bearer "
        Long userId= jwtUtil.extractClaim(token, claims -> claims.get("userId", Long.class));

        return ResponseEntity.ok(userTaskService.addUserTask(userId, userTask));
    }

    // ✅ Get User's Own Tasks
    @GetMapping()
    public ResponseEntity <List <UserTask>> getUserTask( @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7); // Removes "Bearer "
        Long userId= jwtUtil.extractClaim(token, claims -> claims.get("userId", Long.class));

        return ResponseEntity.ok(userTaskService.getUserTasks(userId));
    }

    // ✅ Get User's Own Tasks by id
    @GetMapping("/{taskId}")
    public ResponseEntity  <UserTask> getUserTask(@PathVariable Long taskId, @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7); // Removes "Bearer "
        Long userId= jwtUtil.extractClaim(token, claims -> claims.get("userId", Long.class));

        return ResponseEntity.ok(userTaskService.getUserTasksById(taskId));
    }


    // ✅ Update Task (Only if the user owns it)
    @PutMapping("/{taskId}")
    public ResponseEntity<UserTask> updateUserTask(@PathVariable Long taskId, @RequestBody UserTask userTask, @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7); // Removes "Bearer "
        Long userId= jwtUtil.extractClaim(token, claims -> claims.get("userId", Long.class));

        return ResponseEntity.ok(userTaskService.updateUserTask(taskId, userId, userTask));
    }

    // ✅ Delete Task (Only if the user owns it)
    @DeleteMapping("/{taskId}")
    public ResponseEntity<String> deleteUserTask(@PathVariable Long taskId,
                                               @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7); // Remove "Bearer "
        Long userId = jwtUtil.extractClaim(token, claims -> claims.get("userId", Long.class));

        userTaskService.deleteUserTask(taskId, userId);
        return ResponseEntity.ok("✅ Task deleted successfully.");
    }

    // 👨‍💼 Admin can view tasks of users they have created
    @GetMapping("/admin/view-tasks")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserTask>> getTasksByAdmin(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7); // Remove "Bearer "
        Long adminId = jwtUtil.extractClaim(token, claims -> claims.get("userId", Long.class));

        List<UserTask> tasks = userTaskService.getTasksCreatedByAdmin(adminId);

        return ResponseEntity.ok(tasks);
    }


}
