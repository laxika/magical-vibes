package com.github.laxika.magicalvibes.networking.message;

import com.github.laxika.magicalvibes.networking.model.MessageType;

public class RegisterRequest {

    private MessageType type;
    private String username;
    private String password;
    private String confirmPassword;

    public RegisterRequest() {
    }

    public RegisterRequest(MessageType type, String username, String password, String confirmPassword) {
        this.type = type;
        this.username = username;
        this.password = password;
        this.confirmPassword = confirmPassword;
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}
