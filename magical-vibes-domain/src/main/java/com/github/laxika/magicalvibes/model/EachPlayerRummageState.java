package com.github.laxika.magicalvibes.model;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.UUID;

/**
 * Progress state for an "each player discards any number of cards, then draws that many cards"
 * flow (Flux). The flow is driven one player at a time by
 * {@code EachPlayerDiscardsAnyNumberThenDrawsThatManyEffectHandler}, which re-runs on each
 * X-value choice and on each discard completion (via {@code rerunCurrentEffectAfterInteraction}).
 */
public class EachPlayerRummageState {

    /** Whether a flow is in progress (guards fresh initialization). */
    public boolean active;
    /** The player currently choosing/discarding. */
    public UUID currentPlayerId;
    /** Number of cards the current player is drawing after their discard completes. */
    public int pendingDraw;
    /** Players (APNAP order) not yet processed. */
    public final Deque<UUID> remaining = new ArrayDeque<>();

    public void reset() {
        active = false;
        currentPlayerId = null;
        pendingDraw = 0;
        remaining.clear();
    }
}
