package com.github.laxika.magicalvibes.model.effect;

/**
 * At the beginning of the next end step, sacrifices the targeted permanent if its mana value is at
 * most the configured maximum and the ability's controller still controls it.
 */
public record SacrificeTargetPermanentAtEndStepIfManaValueAtMostEffect(int maxManaValue)
        implements CardEffect {

    public SacrificeTargetPermanentAtEndStepIfManaValueAtMostEffect {
        if (maxManaValue < 0) {
            throw new IllegalArgumentException("maxManaValue must not be negative");
        }
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.creature());
    }
}
