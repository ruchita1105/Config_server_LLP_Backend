package com.UserManagement.service;

import com.UserManagement.model.User;
import com.UserManagement.model.UserTask;
import com.UserManagement.repository.AdminUserRepository;
import com.UserManagement.repository.UserTaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserTaskService {

    @Autowired
    private UserTaskRepository userTaskRepository;

    @Autowired
    private AdminUserRepository userRepository;

    // ✅ Add task (Only to self)
    public UserTask addUserTask(Long userId, UserTask userTask) {


        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException(" User not found"));

        userTask.setUser(user);
        return userTaskRepository.save(userTask);
    }

    // ✅ Get tasks (Only your own)
    public List<UserTask> getUserTasks(Long userId) {


        return userTaskRepository.findByUserId(userId);
    }

    // ✅ Update task (Only if you own it)
    public UserTask updateUserTask(Long taskId, Long userId, UserTask updatedTask) {
        UserTask existingTask = userTaskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("❌ Task not found"));

        if (!existingTask.getUser().getId().equals(userId)) {
            throw new RuntimeException("❌ You are not authorized to update this task");
        }

        existingTask.setTitle(updatedTask.getTitle());
        existingTask.setDescription(updatedTask.getDescription());
        existingTask.setStatus(updatedTask.getStatus());

        return userTaskRepository.save(existingTask);
    }

    // ✅ Delete task (Only if you own it)
    public void deleteUserTask(Long taskId, Long userId) {
        UserTask task = userTaskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("❌ Task not found"));

        if (!task.getUser().getId().equals(userId)) {
            throw new RuntimeException("❌ Unauthorized: You cannot delete this task!");
        }

        userTaskRepository.deleteById(taskId);
    }

    public UserTask getUserTasksById(Long taskId) {
      return   userTaskRepository.findById(taskId).get();
    }

    public List<UserTask> getTasksCreatedByAdmin(Long adminId) {
        // 1. Fetch all users created by this admin
        List<User> usersCreatedByAdmin = userRepository.findByCreatedBy(adminId);

        // 2. Extract user IDs
        List<Long> userIds = usersCreatedByAdmin.stream()
                .map(User::getId)
                .toList();

        // 3. Fetch tasks for these user IDs
        return userTaskRepository.findByUserIdIn(userIds);
    }

}
