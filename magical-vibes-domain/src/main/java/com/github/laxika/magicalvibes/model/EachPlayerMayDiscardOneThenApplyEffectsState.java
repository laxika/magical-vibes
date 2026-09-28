package com.github.laxika.magicalvibes.model;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

/** Progress state for an each-player may-discard-one effect with shared discard-type riders. */
public class EachPlayerMayDiscardOneThenApplyEffectsState {

    public boolean active;
    public UUID controllerId;
    public UUID currentPlayerId;
    public int currentDiscardCountBefore;
    public boolean creatureCardDiscarded;
    public boolean nonCreatureCardDiscarded;
    public final Deque<UUID> remaining = new ArrayDeque<>();
    public final Set<UUID> playersWhoDiscarded = new LinkedHashSet<>();

    public void reset() {
        active = false;
        controllerId = null;
        currentPlayerId = null;
        currentDiscardCountBefore = 0;
        creatureCardDiscarded = false;
        nonCreatureCardDiscarded = false;
        remaining.clear();
        playersWhoDiscarded.clear();
    }
}
