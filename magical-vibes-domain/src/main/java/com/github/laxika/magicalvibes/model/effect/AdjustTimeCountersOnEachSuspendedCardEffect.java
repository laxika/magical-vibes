package com.github.laxika.magicalvibes.model.effect;

/** Adds or removes a fixed number of time counters from suspended cards. */
public record AdjustTimeCountersOnEachSuspendedCardEffect(boolean add, int amount,
                                                          boolean controllerOwnedOnly)
        implements CardEffect {

    public AdjustTimeCountersOnEachSuspendedCardEffect(boolean add) {
        this(add, 2, false);
    }

    public AdjustTimeCountersOnEachSuspendedCardEffect(boolean add, int amount) {
        this(add, amount, false);
    }

    public AdjustTimeCountersOnEachSuspendedCardEffect(boolean add, boolean controllerOwnedOnly) {
        this(add, 2, controllerOwnedOnly);
    }

    public AdjustTimeCountersOnEachSuspendedCardEffect {
        if (amount < 1) {
            throw new IllegalArgumentException("amount must be positive");
        }
    }
}
