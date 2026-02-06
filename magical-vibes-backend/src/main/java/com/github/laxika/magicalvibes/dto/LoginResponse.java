package com.github.laxika.magicalvibes.dto;

public class LoginResponse {

    private String type;
    private String message;
    private Long userId;
    private String username;

    public LoginResponse() {
    }

    public LoginResponse(String type, String message, Long userId, String username) {
        this.type = type;
        this.message = message;
        this.userId = userId;
        this.username = username;
    }

    public static LoginResponse success(Long userId, String username) {
        return new LoginResponse("LOGIN_SUCCESS", "Login successful", userId, username);
    }

    public static LoginResponse failure(String message) {
        return new LoginResponse("LOGIN_FAILURE", message, null, null);
    }

    public static LoginResponse timeout() {
        return new LoginResponse("TIMEOUT", "Connection timeout - no login message received", null, null);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
