package com.github.laxika.magicalvibes.model.effect;

/**
 * When resolved, registers a delayed trigger that fires at the beginning of the next end step:
 * the controller loses {@code lifeLoss} life and the source card returns from the graveyard to
 * their hand (if still there). Both instructions resolve as a single triggered ability.
 *
 * <p>Used by Brood of Cockroaches: "When this creature is put into your graveyard from the
 * battlefield, at the beginning of the next end step, you lose 1 life and return this card to
 * your hand."
 *
 * @param lifeLoss life the controller loses when the delayed trigger resolves
 */
public record RegisterDelayedLoseLifeAndReturnFromGraveyardToHandEffect(int lifeLoss)
        implements CardEffect {
}
