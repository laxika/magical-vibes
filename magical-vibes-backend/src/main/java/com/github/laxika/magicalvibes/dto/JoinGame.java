package com.github.laxika.magicalvibes.dto;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;

import java.util.List;
import java.util.Map;

public record JoinGame(long id, String gameName, GameStatus status,
                       List<String> playerNames, List<Long> playerIds, List<String> gameLog,
                       TurnStep currentStep, Long activePlayerId, int turnNumber, Long priorityPlayerId,
                       List<Card> hand, int mulliganCount, List<Integer> deckSizes,
                       List<List<Permanent>> battlefields, Map<String, Integer> manaPool) {
}
