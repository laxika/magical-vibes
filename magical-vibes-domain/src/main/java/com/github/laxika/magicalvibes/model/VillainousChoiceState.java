package com.github.laxika.magicalvibes.model;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.UUID;

/** Progress state for a sequential villainous choice. */
public class VillainousChoiceState {

    public boolean active;
    public final Deque<UUID> remaining = new ArrayDeque<>();
    public UUID currentPlayerId;
    public UUID currentTargetId;
    /** Number of additional repetitions still owed by a villainous-choice replacement. */
    public int additionalChoicesRemaining = -1;
    public UUID choicePlayerId;
    public String choiceSourceCardName;
    public String choicePutOption;
    public List<String> choiceOptions = List.of();
    public String choiceDescription;
    public String chosenMode;
    public boolean waitingForDiscard;
    public boolean waitingForDraw;
    public boolean waitingForControllerMay;
    public boolean waitingForCardChoice;

    public void reset() {
        active = false;
        remaining.clear();
        currentPlayerId = null;
        currentTargetId = null;
        additionalChoicesRemaining = -1;
        choicePlayerId = null;
        choiceSourceCardName = null;
        choicePutOption = null;
        choiceOptions = List.of();
        choiceDescription = null;
        chosenMode = null;
        waitingForDiscard = false;
        waitingForDraw = false;
        waitingForControllerMay = false;
        waitingForCardChoice = false;
    }
}
