package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Target player may discard up to the evaluated amount of cards, then draws that many cards.
 */
public record TargetPlayerDiscardsUpToThenDrawsThatManyEffect(DynamicAmount maxDiscard)
        implements CardEffect {

    public TargetPlayerDiscardsUpToThenDrawsThatManyEffect(int maxDiscard) {
        this(new Fixed(maxDiscard));
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
