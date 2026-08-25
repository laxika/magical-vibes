package com.github.laxika.magicalvibes.model.effect;

/** Adds time counters to the suspended card represented by the resolving stack entry. */
public record PutTimeCountersOnSuspendedCardEffect(int amount) implements CardEffect {

    public PutTimeCountersOnSuspendedCardEffect {
        if (amount < 1) {
            throw new IllegalArgumentException("amount must be positive");
        }
    }
}
