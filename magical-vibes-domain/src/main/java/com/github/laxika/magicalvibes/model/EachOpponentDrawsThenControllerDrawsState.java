package com.github.laxika.magicalvibes.model;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.UUID;

/** Progress state for an opponent draw sequence followed by matching controller draws. */
public class EachOpponentDrawsThenControllerDrawsState {

    public boolean active;
    public UUID controllerId;
    public final Deque<UUID> remainingOpponentIds = new ArrayDeque<>();
    public final List<UUID> opponentsWhoDrew = new ArrayList<>();
    public UUID currentOpponentId;
    public int cardsDrawnBeforeCurrentOpponent;
    public boolean controllerDrawPending;

    public void reset() {
        active = false;
        controllerId = null;
        remainingOpponentIds.clear();
        opponentsWhoDrew.clear();
        currentOpponentId = null;
        cardsDrawnBeforeCurrentOpponent = 0;
        controllerDrawPending = false;
    }
}
