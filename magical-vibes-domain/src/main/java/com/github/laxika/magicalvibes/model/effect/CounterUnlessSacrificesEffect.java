package com.github.laxika.magicalvibes.model.effect;

/**
 * Counters the targeted spell or ability unless its controller sacrifices a permanent.
 * This is the permanent-sacrifice variant of ward.
 */
public record CounterUnlessSacrificesEffect() implements CounterUnlessEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.spellOnStack());
    }

    @Override
    public RansomKind ransomKind() {
        return RansomKind.SACRIFICE_PERMANENT;
    }

    @Override
    public int ransomMagnitude() {
        return 1;
    }
}
