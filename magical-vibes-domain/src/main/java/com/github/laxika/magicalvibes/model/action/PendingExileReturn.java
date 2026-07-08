package com.github.laxika.magicalvibes.model.action;

import java.util.UUID;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TurnStep;

public record PendingExileReturn(
        Card card,
        UUID controllerId,
        boolean returnTapped,
        boolean returnToHand,
        TurnStep returnStep) implements DelayedAction {

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
