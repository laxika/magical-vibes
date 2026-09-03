package com.github.laxika.magicalvibes.model;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/** Progress state for an each-player discard flow that checks for a discarded creature card. */
public class EachPlayerDiscardsCreatureOrLosesLifeState {

    public boolean active;
    public UUID currentPlayerId;
    public final Deque<UUID> remaining = new ArrayDeque<>();
    public final Set<UUID> playersWhoDiscardedCreature = new HashSet<>();

    public void reset() {
        active = false;
        currentPlayerId = null;
        remaining.clear();
        playersWhoDiscardedCreature.clear();
    }
}
