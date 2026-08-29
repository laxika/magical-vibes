package com.github.laxika.magicalvibes.model;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.UUID;

/** Progress state for an APNAP each-player discard flow with an opponent life-loss fallback. */
public class EachPlayerDiscardsOrLosesLifeState {

    public boolean active;
    public UUID currentPlayerId;
    public boolean discardPending;
    public final Deque<UUID> remaining = new ArrayDeque<>();

    public void reset() {
        active = false;
        currentPlayerId = null;
        discardPending = false;
        remaining.clear();
    }
}
