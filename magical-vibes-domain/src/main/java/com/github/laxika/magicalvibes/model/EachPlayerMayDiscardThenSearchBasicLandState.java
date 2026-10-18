package com.github.laxika.magicalvibes.model;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.UUID;

/** Progress state for each player's optional discard followed by a basic-land search. */
public class EachPlayerMayDiscardThenSearchBasicLandState {

    public boolean active;
    public UUID controllerId;
    public UUID currentPlayerId;
    public final Deque<UUID> remaining = new ArrayDeque<>();

    public void reset() {
        active = false;
        controllerId = null;
        currentPlayerId = null;
        remaining.clear();
    }
}
