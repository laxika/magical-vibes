package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * Returns the source card from its owner's graveyard to the battlefield transformed,
 * immediately on resolution. By default it returns under the trigger controller's control;
 * {@code underOwnersControl} handles effects that explicitly return it under its owner's control.
 *
 * <p>Unlike {@link RegisterDelayedReturnSourceTransformedEffect} there is no wait for the
 * next end step.
 */
public record ReturnSourceTransformedFromGraveyardEffect(
        boolean tapped,
        CounterType enteringCounterType,
        int enteringCounterCount,
        boolean underOwnersControl
) implements CardEffect {

    public ReturnSourceTransformedFromGraveyardEffect() {
        this(false, null, 0, false);
    }

    public ReturnSourceTransformedFromGraveyardEffect(boolean tapped) {
        this(tapped, null, 0, false);
    }

    public ReturnSourceTransformedFromGraveyardEffect(boolean tapped, boolean underOwnersControl) {
        this(tapped, null, 0, underOwnersControl);
    }

    public ReturnSourceTransformedFromGraveyardEffect(boolean tapped, CounterType enteringCounterType,
                                                      int enteringCounterCount) {
        this(tapped, enteringCounterType, enteringCounterCount, false);
    }

    public ReturnSourceTransformedFromGraveyardEffect {
        if (enteringCounterCount < 0) {
            throw new IllegalArgumentException("Entering counter count cannot be negative");
        }
        if (enteringCounterCount > 0 && enteringCounterType == null) {
            throw new IllegalArgumentException("Entering counter type is required when counters are added");
        }
    }
}
