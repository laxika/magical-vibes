package com.github.laxika.magicalvibes.model.effect;

/**
 * Counters target spell, then makes each matching card in that spell controller's graveyard,
 * hand, and library perpetually cost the supplied amount more to cast.
 */
public record CounterSpellAndPerpetuallyIncreaseSameNameCastCostEffect(int amount)
        implements CounterSpellingEffect {

    public CounterSpellAndPerpetuallyIncreaseSameNameCastCostEffect {
        if (amount <= 0) {
            throw new IllegalArgumentException("Perpetual cast-cost increase must be positive");
        }
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.spellOnStack());
    }
}
