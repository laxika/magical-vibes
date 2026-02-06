package com.github.laxika.magicalvibes.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class LoginResponse {

    private String type;
    private String message;
    private Long userId;
    private String username;
    private List<GameResponse> games;

    public LoginResponse() {
    }

    public LoginResponse(String type, String message, Long userId, String username, List<GameResponse> games) {
        this.type = type;
        this.message = message;
        this.userId = userId;
        this.username = username;
        this.games = games;
    }

    public static LoginResponse success(Long userId, String username, List<GameResponse> games) {
        return new LoginResponse("LOGIN_SUCCESS", "Login successful", userId, username, games);
    }

    public static LoginResponse failure(String message) {
        return new LoginResponse("LOGIN_FAILURE", message, null, null, null);
    }

    public static LoginResponse timeout() {
        return new LoginResponse("TIMEOUT", "Connection timeout - no login message received", null, null, null);
    }

}
