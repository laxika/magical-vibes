package com.github.laxika.magicalvibes.model;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Progress state for Cait's two-player draw/discard mana-value comparison. */
public class CaitCageBrawlerState {

    public boolean active;
    public UUID controllerId;
    public UUID defendingPlayerId;
    public UUID currentPlayerId;
    public final Deque<UUID> remaining = new ArrayDeque<>();
    public final Map<UUID, Integer> discardedManaValues = new HashMap<>();

    public void reset() {
        active = false;
        controllerId = null;
        defendingPlayerId = null;
        currentPlayerId = null;
        remaining.clear();
        discardedManaValues.clear();
    }
}
