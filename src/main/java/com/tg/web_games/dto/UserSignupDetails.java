package com.tg.web_games.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserSignupDetails {
    @NotBlank @Size(min = 2)
    private String firstName;
    @NotBlank @Size(min = 2)
    private String lastName;
    @NotBlank @Size(min = 3)
    private String userName;
    @NotBlank @Email @Size(min = 8)
    private String emailAddress;
    @NotBlank @Size(min = 8)
    private String passwordDetails;
}
