package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/** Earthbends a land, then creates the reflexive fight ability when the action succeeds. */
public record EarthbendTargetLandThenFightEffect(DynamicAmount counterCount) implements CardEffect {

    public EarthbendTargetLandThenFightEffect(int counterCount) {
        this(new Fixed(counterCount));
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.land());
    }
}
