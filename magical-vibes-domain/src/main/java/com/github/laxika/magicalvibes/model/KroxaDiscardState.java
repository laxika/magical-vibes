package com.github.laxika.magicalvibes.model;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Progress state for Kroxa's opponent discard and nonland comparison. */
public class KroxaDiscardState {

    public boolean active;
    public UUID controllerId;
    public UUID currentPlayerId;
    public final Deque<UUID> remaining = new ArrayDeque<>();
    public final Map<UUID, Boolean> discardedNonland = new HashMap<>();

    public void reset() {
        active = false;
        controllerId = null;
        currentPlayerId = null;
        remaining.clear();
        discardedNonland.clear();
    }
}
