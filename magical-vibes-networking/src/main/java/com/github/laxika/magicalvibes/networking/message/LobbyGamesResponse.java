package com.github.laxika.magicalvibes.networking.message;

import com.github.laxika.magicalvibes.networking.model.MessageType;

import java.util.List;

public record LobbyGamesResponse(MessageType type, List<LobbyGame> games) {

    public LobbyGamesResponse(List<LobbyGame> games) {
        this(MessageType.LOBBY_GAMES_RESPONSE, games);
    }
}
