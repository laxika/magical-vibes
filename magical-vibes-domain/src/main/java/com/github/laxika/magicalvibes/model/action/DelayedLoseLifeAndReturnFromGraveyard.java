package com.github.laxika.magicalvibes.model.action;

import com.github.laxika.magicalvibes.model.Card;

import java.util.UUID;

/**
 * Delayed trigger: at the beginning of the next end step, the controller loses {@code lifeLoss}
 * life and the source card is returned from its owner's graveyard to their hand (if still there).
 * Used by Brood of Cockroaches.
 */
public record DelayedLoseLifeAndReturnFromGraveyard(
        UUID controllerId, Card sourceCard, int lifeLoss) implements DelayedAction {
}
