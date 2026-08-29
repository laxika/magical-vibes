package com.github.laxika.magicalvibes.model.effect;

/** Counter target spell or ability unless its controller collects evidence. */
public record CounterUnlessCollectsEvidenceEffect(int minimumManaValue)
        implements CounterSpellingEffect, CounterUnlessEffect {

    public CounterUnlessCollectsEvidenceEffect {
        if (minimumManaValue < 0) {
            throw new IllegalArgumentException("minimumManaValue cannot be negative");
        }
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.spellOnStack());
    }

    @Override
    public RansomKind ransomKind() {
        return RansomKind.COLLECT_EVIDENCE;
    }

    @Override
    public int ransomMagnitude() {
        return minimumManaValue;
    }
}
