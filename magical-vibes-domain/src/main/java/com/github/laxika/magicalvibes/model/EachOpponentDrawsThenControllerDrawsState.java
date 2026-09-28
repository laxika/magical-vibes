package com.github.laxika.magicalvibes.model;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.UUID;

/** Progress state for an opponent draw sequence with a matching controller draw. */
public class EachOpponentDrawsThenControllerDrawsState {

    public boolean active;
    public UUID controllerId;
    public final Deque<UUID> remainingOpponentIds = new ArrayDeque<>();
    public UUID pendingOpponentId;
    public int pendingDrawCount;
    public int successfulDrawCount;

    public void reset() {
        active = false;
        controllerId = null;
        remainingOpponentIds.clear();
        pendingOpponentId = null;
        pendingDrawCount = 0;
        successfulDrawCount = 0;
    }
}
