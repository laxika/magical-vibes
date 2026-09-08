package com.github.laxika.magicalvibes.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Mutable state for Queen Kayla bin-Kroog's multi-step activated ability. */
public class QueenKaylaBinKroogOperationState {

    public boolean active;
    public UUID controllerId;
    public final List<UUID> discardedCardIds = new ArrayList<>();
    public final List<UUID> chosenCardIds = new ArrayList<>();
    public int nextManaValue = 1;
    public boolean awaitingChoice;
    public boolean choiceMade;
    public UUID chosenCardId;

    public void reset() {
        active = false;
        controllerId = null;
        discardedCardIds.clear();
        chosenCardIds.clear();
        nextManaValue = 1;
        awaitingChoice = false;
        choiceMade = false;
        chosenCardId = null;
    }
}
