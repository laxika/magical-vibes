package com.github.laxika.magicalvibes.model.planar;

import com.github.laxika.magicalvibes.model.Card;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Mutable planar state, accessed under the owning game's monitor. */
public final class PlanechaseState {
    public final List<Card> deck = new ArrayList<>();
    public final List<PlanarObject> faceUp = new ArrayList<>();
    public final Map<UUID, Integer> specialActionRolls = new HashMap<>();
    public UUID controllerId;
    public int rollTurn = -1;
    public long rollSequence;
    public PlanarDieResult lastRoll;
    public UUID lastRollPlayerId;

    public int rollCost(UUID playerId, int turn) {
        return rollTurn == turn ? specialActionRolls.getOrDefault(playerId, 0) : 0;
    }

    public void recordSpecialAction(UUID playerId, int turn) {
        if (rollTurn != turn) {
            specialActionRolls.clear();
            rollTurn = turn;
        }
        specialActionRolls.merge(playerId, 1, Integer::sum);
    }

    public PlanechaseState copy() {
        PlanechaseState copy = new PlanechaseState();
        copy.deck.addAll(deck);
        faceUp.forEach(object -> copy.faceUp.add(object.copy()));
        copy.specialActionRolls.putAll(specialActionRolls);
        copy.controllerId = controllerId;
        copy.rollTurn = rollTurn;
        copy.rollSequence = rollSequence;
        copy.lastRoll = lastRoll;
        copy.lastRollPlayerId = lastRollPlayerId;
        return copy;
    }
}
