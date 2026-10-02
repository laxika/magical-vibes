package com.github.laxika.magicalvibes.model;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

/** Progress state for each player's optional discard followed by a basic-land search. */
public class EachPlayerMayDiscardThenSearchBasicLandState {

    public boolean active;
    public UUID controllerId;
    public UUID currentPlayerId;
    public int currentDiscardCountBefore;
    public final Deque<UUID> remaining = new ArrayDeque<>();
    public final Set<UUID> playersWhoDiscarded = new LinkedHashSet<>();

    public void reset() {
        active = false;
        controllerId = null;
        currentPlayerId = null;
        currentDiscardCountBefore = 0;
        remaining.clear();
        playersWhoDiscarded.clear();
    }
}
