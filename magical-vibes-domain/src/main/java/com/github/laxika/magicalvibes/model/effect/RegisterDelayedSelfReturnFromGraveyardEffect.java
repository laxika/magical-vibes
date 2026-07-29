package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * Registers a delayed trigger that returns the source card from its owner's graveyard to the
 * battlefield under that owner's control at the beginning of the next end step, entering with
 * {@code counterAmount} counters of {@code counterType}.
 *
 * <p>Sand Golem: "return this card from your graveyard to the battlefield with a +1/+1 counter on it
 * at the beginning of the next end step." Registration fizzles when the card is no longer in a
 * graveyard, and the delayed return itself is skipped if the card has left the graveyard by then.
 *
 * @param counterType   counter to enter with, or {@code null} for none
 * @param counterAmount number of counters (ignored when {@code counterType} is null)
 */
public record RegisterDelayedSelfReturnFromGraveyardEffect(
        CounterType counterType,
        int counterAmount
) implements CardEffect {

    /** No counters. */
    public RegisterDelayedSelfReturnFromGraveyardEffect() {
        this(null, 0);
    }
}
