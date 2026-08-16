package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/** Counter target spell or ability unless its controller pays a dynamic amount of life. */
public record CounterUnlessPaysLifeEffect(DynamicAmount amount)
        implements CounterSpellingEffect, CounterUnlessEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.spellOnStack());
    }

    @Override
    public RansomKind ransomKind() {
        return RansomKind.PAY_LIFE;
    }

    @Override
    public int ransomMagnitude() {
        return 0;
    }
}
