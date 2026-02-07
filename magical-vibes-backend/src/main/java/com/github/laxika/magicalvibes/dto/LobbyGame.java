package com.github.laxika.magicalvibes.dto;

import com.github.laxika.magicalvibes.model.GameStatus;

public record LobbyGame(long id, String gameName, String createdByUsername,
                         int playerCount, GameStatus status) {
}
