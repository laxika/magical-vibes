package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * The target opponent chooses to sacrifice a creature, pay life, or let the controller draw a
 * card. The actual choice is resolved by the effect handler because the sacrifice option needs a
 * follow-up permanent choice.
 */
public record DrawCardUnlessTargetSacrificesCreatureOrPaysLifeEffect(int lifeCost)
        implements CardDrawingEffect {

    @Override
    public DynamicAmount drawnCardAmount() {
        return new Fixed(1);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetCategory.PLAYER);
    }
}
