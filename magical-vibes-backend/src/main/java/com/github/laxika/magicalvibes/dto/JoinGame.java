package com.github.laxika.magicalvibes.dto;

import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.TurnStep;

import java.util.List;

public record JoinGame(long id, String gameName, GameStatus status,
                       List<String> playerNames, List<Long> playerIds, List<String> gameLog,
                       TurnStep currentStep, Long activePlayerId, int turnNumber, Long priorityPlayerId) {
}
