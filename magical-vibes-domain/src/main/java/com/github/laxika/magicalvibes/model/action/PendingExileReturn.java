package com.github.laxika.magicalvibes.model.action;

import java.util.UUID;
import java.util.List;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TurnStep;

/**
 * Delayed return of exiled cards to the battlefield, hand, or graveyard at the beginning of
 * {@code returnStep}.
 *
 * <p>{@code onlyOnControllersTurn} narrows "at the beginning of the next X" to "at the beginning of
 * <em>your</em> next X" — the return waits for a step of the given kind on the controller's own turn
 * (Obzedat, Ghost Council). {@code grantHaste} gives the returning permanent haste for as long as it
 * remains on the battlefield.
 */
public record PendingExileReturn(
        Card card,
        UUID controllerId,
        boolean returnTapped,
        boolean returnToHand,
        TurnStep returnStep,
        int plusOnePlusOneCounters,
        List<Card> additionalCards,
        boolean onlyOnControllersTurn,
        boolean grantHaste,
        boolean returnAttacking,
        boolean returnToGraveyard,
        UUID timingControllerId,
        Card followUpSourceCard,
        boolean discardControllerCardsEqualToReturnedToughness) implements DelayedAction {

    public PendingExileReturn {
        additionalCards = additionalCards == null ? List.of() : List.copyOf(additionalCards);
    }

    public PendingExileReturn(Card card, UUID controllerId, boolean returnTapped, boolean returnToHand,
                              TurnStep returnStep, int plusOnePlusOneCounters, List<Card> additionalCards) {
        this(card, controllerId, returnTapped, returnToHand, returnStep, plusOnePlusOneCounters,
                additionalCards, false, false, false, false, null, null, false);
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

    public PendingExileReturn(Card card, UUID controllerId, boolean returnTapped, boolean returnToHand,
                              TurnStep returnStep, int plusOnePlusOneCounters, List<Card> additionalCards,
                              boolean onlyOnControllersTurn, boolean grantHaste) {
        this(card, controllerId, returnTapped, returnToHand, returnStep, plusOnePlusOneCounters,
                additionalCards, onlyOnControllersTurn, grantHaste, false, false,
                null, null, false);
    }

    public PendingExileReturn(Card card, UUID controllerId, boolean returnTapped, boolean returnToHand,
                              TurnStep returnStep, int plusOnePlusOneCounters, List<Card> additionalCards,
                              boolean onlyOnControllersTurn, boolean grantHaste, boolean returnAttacking) {
        this(card, controllerId, returnTapped, returnToHand, returnStep, plusOnePlusOneCounters,
                additionalCards, onlyOnControllersTurn, grantHaste, returnAttacking, false,
                null, null, false);
    }

    public static PendingExileReturn toGraveyard(Card card, UUID ownerId) {
        return new PendingExileReturn(card, ownerId, false, false, TurnStep.END_STEP, 0, List.of(),
                false, false, false, true, null, null, false);
    }

    public static PendingExileReturn forControllerNextUpkeepWithToughnessDiscard(
            Card card, UUID ownerId, UUID timingControllerId, Card sourceCard, List<Card> additionalCards) {
        return new PendingExileReturn(card, ownerId, false, false, TurnStep.UPKEEP, 0, additionalCards,
                true, false, false, false, timingControllerId, sourceCard, true);
    }
}
