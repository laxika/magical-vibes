package com.github.laxika.magicalvibes.model.action;

import java.util.UUID;
import java.util.List;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TurnStep;

public record PendingExileReturn(
        Card card,
        UUID controllerId,
        boolean returnTapped,
        boolean returnToHand,
        TurnStep returnStep,
        int plusOnePlusOneCounters,
        List<Card> additionalCards) implements DelayedAction {

    public PendingExileReturn {
        additionalCards = additionalCards == null ? List.of() : List.copyOf(additionalCards);
    }

    public PendingExileReturn(Card card, UUID controllerId) {
        this(card, controllerId, false, false, TurnStep.END_STEP, 0, List.of());
    }

    public PendingExileReturn(Card card, UUID controllerId, boolean returnTapped) {
        this(card, controllerId, returnTapped, false, TurnStep.END_STEP, 0, List.of());
    }

    public PendingExileReturn(Card card, UUID controllerId, boolean returnTapped, boolean returnToHand) {
        this(card, controllerId, returnTapped, returnToHand, TurnStep.END_STEP, 0, List.of());
    }

    public PendingExileReturn(Card card, UUID controllerId, boolean returnTapped, boolean returnToHand,
                              TurnStep returnStep) {
        this(card, controllerId, returnTapped, returnToHand, returnStep, 0, List.of());
    }

    public PendingExileReturn(Card card, UUID controllerId, boolean returnTapped, boolean returnToHand,
                              TurnStep returnStep, int plusOnePlusOneCounters) {
        this(card, controllerId, returnTapped, returnToHand, returnStep, plusOnePlusOneCounters, List.of());
    }
}
