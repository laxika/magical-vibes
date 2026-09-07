package com.github.laxika.magicalvibes.model.effect;

/** Resolves a choice to collect evidence from the controller's graveyard. */
public record CollectEvidenceEffect(int minimumManaValue, CardEffect thenEffect) implements CardEffect {

    public CollectEvidenceEffect(int minimumManaValue) {
        this(minimumManaValue, null);
    }

    public CollectEvidenceEffect {
        if (minimumManaValue < 0) {
            throw new IllegalArgumentException("minimumManaValue cannot be negative");
        }
    }
}
