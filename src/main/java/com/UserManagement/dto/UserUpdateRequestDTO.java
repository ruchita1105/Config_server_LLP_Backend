package com.UserManagement.dto;

import lombok.Data;

@Data
public class UserUpdateRequestDTO {
    private String username;
    private String firstname;
    private String lastname;
    private String password;
    private String role;
}
