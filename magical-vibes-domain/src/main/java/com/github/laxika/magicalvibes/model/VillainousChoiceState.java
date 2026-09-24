package com.github.laxika.magicalvibes.model;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.UUID;

/** Progress state for an opponent-by-opponent villainous choice. */
public class VillainousChoiceState {

    public boolean active;
    public final Deque<UUID> remaining = new ArrayDeque<>();
    public UUID currentPlayerId;
    public String chosenMode;
    public boolean waitingForDiscard;
    public boolean waitingForControllerMay;
    public boolean waitingForCardChoice;

    public void reset() {
        active = false;
        remaining.clear();
        currentPlayerId = null;
        chosenMode = null;
        waitingForDiscard = false;
        waitingForControllerMay = false;
        waitingForCardChoice = false;
    }
}
