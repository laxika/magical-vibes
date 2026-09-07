package com.github.laxika.magicalvibes.model;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.UUID;

/** Progress state for an opponent-by-opponent discard-two-unless-nonland sequence. */
public class EachOpponentDiscardsTwoUnlessNonlandState {

    public boolean active;
    public final Deque<UUID> remainingOpponentIds = new ArrayDeque<>();
    public UUID currentOpponentId;
    public boolean awaitingMayChoice;
    public boolean awaitingDiscard;

    public void reset() {
        active = false;
        remainingOpponentIds.clear();
        currentOpponentId = null;
        awaitingMayChoice = false;
        awaitingDiscard = false;
    }
}
