package com.github.laxika.magicalvibes.networking.model;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.planar.PlanarDieResult;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record PlanechaseView(List<PlanarCardView> faceUp, UUID controllerId, int deckSize,
                             int rollCost, boolean canRoll, boolean canPayRoll,
                             PlanarDieResult lastRoll, UUID lastRollPlayerId, long rollSequence) {
    public record PlanarCardView(UUID id, CardView card, Map<CounterType, Integer> counters,
                                 List<Integer> availableAbilityIndices) {}
}
