package com.github.laxika.magicalvibes.model;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.UUID;

/**
 * Progress state for placing the cards revealed by a clash (CR 701.30c). Players decide in APNAP
 * order whether their revealed card goes to the bottom of their library; the decisions are
 * collected here and every card moves together once the last player has decided. Driven by
 * {@code PlaceClashCardsEffectHandler}, which re-runs on each answered placement prompt.
 */
public class ClashPlacementState {

    /** Whether a placement is in progress (guards fresh initialization). */
    public boolean active;
    /** The player currently deciding where their revealed card goes. */
    public UUID currentPlayerId;
    /** Clashing players (APNAP order) not yet asked. */
    public final Deque<UUID> remaining = new ArrayDeque<>();
    /** Decisions made so far, in APNAP order. */
    public final List<Placement> placements = new ArrayList<>();

    public void reset() {
        active = false;
        currentPlayerId = null;
        remaining.clear();
        placements.clear();
    }

    public record Placement(UUID playerId, UUID cardId, boolean toBottom) {
    }
}
