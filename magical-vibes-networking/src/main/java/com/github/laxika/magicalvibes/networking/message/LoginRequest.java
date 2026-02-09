package com.github.laxika.magicalvibes.networking.message;

import com.github.laxika.magicalvibes.networking.model.MessageType;

public class LoginRequest {

    private MessageType type;
    private String username;
    private String password;

    public LoginRequest() {
    }

    public LoginRequest(MessageType type, String username, String password) {
        this.type = type;
        this.username = username;
        this.password = password;
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
}
