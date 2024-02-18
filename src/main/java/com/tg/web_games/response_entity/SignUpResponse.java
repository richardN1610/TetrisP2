package com.tg.web_games.response_entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignUpResponse {
    private String firstName;
    private String lastName;
    private String userName;
    private String emailAddress;
    private String ouptutMsg;
}
