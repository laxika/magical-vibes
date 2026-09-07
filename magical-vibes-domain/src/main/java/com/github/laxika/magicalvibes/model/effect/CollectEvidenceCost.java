package com.github.laxika.magicalvibes.model.effect;

/**
 * Cost that requires exiling any number of cards from the controller's graveyard with total mana
 * value at least the specified amount.
 */
public record CollectEvidenceCost(int minimumManaValue, boolean optional, boolean targetManaValue)
        implements CostEffect {

    public CollectEvidenceCost(int minimumManaValue) {
        this(minimumManaValue, false, false);
    }

    public CollectEvidenceCost(int minimumManaValue, boolean optional) {
        this(minimumManaValue, optional, false);
    }

    public static CollectEvidenceCost forTargetManaValue() {
        return new CollectEvidenceCost(0, false, true);
    }

    public boolean usesTargetManaValue() {
        return targetManaValue;
    }

    public CollectEvidenceCost {
        if (minimumManaValue < 0) {
            throw new IllegalArgumentException("Minimum evidence mana value cannot be negative");
        }
    }
}
