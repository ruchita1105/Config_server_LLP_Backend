package com.UserManagement.repository;

import com.UserManagement.model.User;
import com.UserManagement.model.UserTask;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserTaskRepository extends JpaRepository<UserTask, Long> {

    // ✅ Fetch tasks by userId (used for frontend access)
    List<UserTask> findByUserId(Long userId);

    // ✅ Optional: Fetch tasks by User object (useful if you have a User entity)
    List<UserTask> findByUser(User user);

    List<UserTask> findByUserIdIn(List<Long> userIds);

}
