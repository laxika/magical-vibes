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
 * <p>Phytotitan uses {@link #tappedAtOwnersNextUpkeep()}: "return it to the battlefield tapped under
 * its owner's control at the beginning of their next upkeep."
 *
 * <p>Breathkeeper Seraph uses {@link #returningAtControllersNextUpkeep()}: the card returns under its owner's
 * control at the beginning of the next upkeep of the player who controlled it when it died.
 *
 * @param counterType             counter to enter with, or {@code null} for none
 * @param counterAmount           number of counters (ignored when {@code counterType} is null)
 * @param atNextUpkeep            {@code true} to return at an upkeep instead of the next end step
 * @param tapped                  {@code true} to have it enter the battlefield tapped
 * @param atControllersNextUpkeep with {@code atNextUpkeep}, {@code true} to time the upkeep by the player
 *                                who controlled the card when it died rather than by its owner
 */
public record RegisterDelayedSelfReturnFromGraveyardEffect(
        CounterType counterType,
        int counterAmount,
        boolean atNextUpkeep,
        boolean tapped,
        boolean atControllersNextUpkeep
) implements CardEffect {

    /** No counters, next end step, untapped. */
    public RegisterDelayedSelfReturnFromGraveyardEffect() {
        this(null, 0, false, false);
    }

    /** No counters, next end step. */
    public RegisterDelayedSelfReturnFromGraveyardEffect(CounterType counterType, int counterAmount) {
        this(counterType, counterAmount, false, false);
    }

    public RegisterDelayedSelfReturnFromGraveyardEffect(CounterType counterType, int counterAmount,
                                                        boolean atNextUpkeep, boolean tapped) {
        this(counterType, counterAmount, atNextUpkeep, tapped, false);
    }

    /** Returns tapped at the beginning of the owner's next upkeep (Phytotitan). */
    public static RegisterDelayedSelfReturnFromGraveyardEffect tappedAtOwnersNextUpkeep() {
        return new RegisterDelayedSelfReturnFromGraveyardEffect(null, 0, true, true);
    }

    /** Returns at the beginning of the next upkeep of the player who controlled it when it died (Breathkeeper Seraph). */
    public static RegisterDelayedSelfReturnFromGraveyardEffect returningAtControllersNextUpkeep() {
        return new RegisterDelayedSelfReturnFromGraveyardEffect(null, 0, true, false, true);
    }
}
