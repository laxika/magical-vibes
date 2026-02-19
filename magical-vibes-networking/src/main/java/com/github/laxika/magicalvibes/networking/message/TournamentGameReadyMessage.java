package com.github.laxika.magicalvibes.networking.message;

import com.github.laxika.magicalvibes.networking.model.MessageType;

import java.util.UUID;

public record TournamentGameReadyMessage(
        MessageType type,
        UUID gameId,
        String opponentName
) {
    public TournamentGameReadyMessage(UUID gameId, String opponentName) {
        this(MessageType.TOURNAMENT_GAME_READY, gameId, opponentName);
    }
}
