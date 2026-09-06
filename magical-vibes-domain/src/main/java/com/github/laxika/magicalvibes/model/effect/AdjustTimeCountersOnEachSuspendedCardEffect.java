package com.github.laxika.magicalvibes.model.effect;

/** Adds or removes a fixed number of time counters from each suspended card. */
public record AdjustTimeCountersOnEachSuspendedCardEffect(boolean add, int amount) implements CardEffect {

    public AdjustTimeCountersOnEachSuspendedCardEffect(boolean add) {
        this(add, 2);
    }

    public AdjustTimeCountersOnEachSuspendedCardEffect {
        if (amount < 1) {
            throw new IllegalArgumentException("amount must be positive");
        }
    }
}
