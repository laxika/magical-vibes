package com.github.laxika.magicalvibes.networking.message;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TurnStep;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record JoinGame(UUID id, String gameName, GameStatus status,
                       List<String> playerNames, List<UUID> playerIds, List<String> gameLog,
                       TurnStep currentStep, UUID activePlayerId, int turnNumber, UUID priorityPlayerId,
                       List<Card> hand, int mulliganCount, List<Integer> deckSizes,
                       List<List<Permanent>> battlefields, Map<String, Integer> manaPool,
                       List<TurnStep> autoStopSteps, List<Integer> lifeTotals,
                       List<StackEntry> stack, List<List<Card>> graveyards) {
}
