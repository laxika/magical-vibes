package com.github.laxika.magicalvibes.model.effect;

/**
 * Resolves a choice to collect evidence from the controller's graveyard.
 *
 * <p>With {@code chooseX} the effect is "collect evidence X": the controller first chooses X (up to
 * the total mana value of their graveyard), then exiles cards with total mana value X or greater.
 * The stack entry's event value is X, not the total mana value actually exiled.</p>
 */
public record CollectEvidenceEffect(int minimumManaValue, CardEffect thenEffect, boolean chooseX)
        implements CardEffect {

    public CollectEvidenceEffect(int minimumManaValue, CardEffect thenEffect) {
        this(minimumManaValue, thenEffect, false);
    }

    public CollectEvidenceEffect(int minimumManaValue) {
        this(minimumManaValue, null, false);
    }

    public CollectEvidenceEffect {
        if (minimumManaValue < 0) {
            throw new IllegalArgumentException("minimumManaValue cannot be negative");
        }
    }

    /** "Collect evidence X" with X chosen as the effect resolves. */
    public static CollectEvidenceEffect withChosenX() {
        return new CollectEvidenceEffect(0, null, true);
    }
}
