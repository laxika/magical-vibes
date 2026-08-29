package com.github.laxika.magicalvibes.model.effect;

/**
 * Counters the targeted spell or ability unless its controller pays a generic mana amount using
 * waterbend, allowing untapped artifacts and creatures they control to replace generic mana.
 */
public record CounterUnlessWaterbendsEffect(int amount)
        implements CounterUnlessEffect, CounterSpellingEffect {

    public CounterUnlessWaterbendsEffect {
        if (amount <= 0) {
            throw new IllegalArgumentException("Waterbend amount must be positive");
        }
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.spellOnStack());
    }

    @Override
    public RansomKind ransomKind() {
        return RansomKind.PAY_WATERBEND;
    }

    @Override
    public int ransomMagnitude() {
        return amount;
    }
}
