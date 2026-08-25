package com.github.laxika.magicalvibes.model.effect;

/**
 * Counters target spell unless its controller discards their hand.
 */
public record CounterUnlessDiscardsHandEffect() implements CounterSpellingEffect, CounterUnlessEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.spellOnStack());
    }

    @Override
    public RansomKind ransomKind() {
        return RansomKind.DISCARD_HAND;
    }

    @Override
    public int ransomMagnitude() {
        return 0;
    }
}
