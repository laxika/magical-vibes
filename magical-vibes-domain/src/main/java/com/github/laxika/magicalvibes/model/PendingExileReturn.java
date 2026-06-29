package com.github.laxika.magicalvibes.model;

import java.util.UUID;

public record PendingExileReturn(
        Card card,
        UUID controllerId,
        boolean returnTapped,
        boolean returnToHand,
        TurnStep returnStep) {

    public PendingExileReturn(Card card, UUID controllerId) {
        this(card, controllerId, false, false, TurnStep.END_STEP);
    }

    public PendingExileReturn(Card card, UUID controllerId, boolean returnTapped) {
        this(card, controllerId, returnTapped, false, TurnStep.END_STEP);
    }

    public PendingExileReturn(Card card, UUID controllerId, boolean returnTapped, boolean returnToHand) {
        this(card, controllerId, returnTapped, returnToHand, TurnStep.END_STEP);
    }
}
