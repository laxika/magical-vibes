package com.github.laxika.magicalvibes.networking.message;

import com.github.laxika.magicalvibes.networking.model.MessageType;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class LoginResponse {

    private MessageType type;
    private String message;
    private UUID userId;
    private String username;
    private List<LobbyGame> games;
    private List<DeckInfo> decks;

    public LoginResponse() {
    }

    public LoginResponse(MessageType type, String message, UUID userId, String username, List<LobbyGame> games, List<DeckInfo> decks) {
        this.type = type;
        this.message = message;
        this.userId = userId;
        this.username = username;
        this.games = games;
        this.decks = decks;
    }

    public static LoginResponse success(UUID userId, String username, List<LobbyGame> games, List<DeckInfo> decks) {
        return new LoginResponse(MessageType.LOGIN_SUCCESS, "Login successful", userId, username, games, decks);
    }

    public static LoginResponse failure(String message) {
        return new LoginResponse(MessageType.LOGIN_FAILURE, message, null, null, null, null);
    }

    public static LoginResponse timeout() {
        return new LoginResponse(MessageType.TIMEOUT, "Connection timeout - no login message received", null, null, null, null);
    }
}
