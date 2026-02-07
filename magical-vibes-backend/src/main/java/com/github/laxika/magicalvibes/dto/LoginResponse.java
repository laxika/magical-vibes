package com.github.laxika.magicalvibes.dto;

import com.github.laxika.magicalvibes.model.MessageType;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class LoginResponse {

    private MessageType type;
    private String message;
    private Long userId;
    private String username;
    private List<LobbyGame> games;

    public LoginResponse() {
    }

    public LoginResponse(MessageType type, String message, Long userId, String username, List<LobbyGame> games) {
        this.type = type;
        this.message = message;
        this.userId = userId;
        this.username = username;
        this.games = games;
    }

    public static LoginResponse success(Long userId, String username, List<LobbyGame> games) {
        return new LoginResponse(MessageType.LOGIN_SUCCESS, "Login successful", userId, username, games);
    }

    public static LoginResponse failure(String message) {
        return new LoginResponse(MessageType.LOGIN_FAILURE, message, null, null, null);
    }

    public static LoginResponse timeout() {
        return new LoginResponse(MessageType.TIMEOUT, "Connection timeout - no login message received", null, null, null);
    }

}
