package com.UserManagement.dto;

import lombok.Data;

@Data

public class UserDTO {
    private String username;
    private String firstname;
    private String lastname;
    private String role;
    private String password;
}
