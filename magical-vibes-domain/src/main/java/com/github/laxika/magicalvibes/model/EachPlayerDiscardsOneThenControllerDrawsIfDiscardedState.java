package com.github.laxika.magicalvibes.model;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.UUID;

/** Progress state for collecting one discard from each player before the controller draws. */
public class EachPlayerDiscardsOneThenControllerDrawsIfDiscardedState {

    public boolean active;
    public UUID controllerId;
    public UUID currentPlayerId;
    public int controllerDiscardCountBefore;
    public boolean controllerDiscarded;
    public final Deque<UUID> remaining = new ArrayDeque<>();

    public void reset() {
        active = false;
        controllerId = null;
        currentPlayerId = null;
        controllerDiscardCountBefore = 0;
        controllerDiscarded = false;
        remaining.clear();
    }
}
