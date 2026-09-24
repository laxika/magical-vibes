package com.github.laxika.magicalvibes.model.effect;

/** Counter target spell or ability unless its controller blights a creature they control. */
public record CounterUnlessBlightsEffect(int count)
        implements CounterSpellingEffect, CounterUnlessEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.spellOnStack());
    }

    @Override
    public RansomKind ransomKind() {
        return RansomKind.BLIGHT;
    }

    @Override
    public int ransomMagnitude() {
        return count;
    }
}
