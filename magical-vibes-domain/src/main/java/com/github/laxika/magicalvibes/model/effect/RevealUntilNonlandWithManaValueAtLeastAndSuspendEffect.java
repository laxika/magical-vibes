package com.github.laxika.magicalvibes.model.effect;

/** Reveals until a qualifying nonland card, exiles it with suspend, and bottoms the rest randomly. */
public record RevealUntilNonlandWithManaValueAtLeastAndSuspendEffect(
        int minimumManaValue,
        int timeCounters
) implements CardEffect {

    public RevealUntilNonlandWithManaValueAtLeastAndSuspendEffect {
        if (minimumManaValue < 0) {
            throw new IllegalArgumentException("minimumManaValue cannot be negative");
        }
        if (timeCounters < 1) {
            throw new IllegalArgumentException("timeCounters must be positive");
        }
    }
}
