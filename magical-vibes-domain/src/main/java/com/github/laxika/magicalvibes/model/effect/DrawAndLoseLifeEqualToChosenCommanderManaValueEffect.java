package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.EventValue;

/**
 * At resolution, the controller chooses one of their commanders on the battlefield or in the
 * command zone, then draws cards and loses life equal to that commander's mana value.
 */
public record DrawAndLoseLifeEqualToChosenCommanderManaValueEffect() implements CardDrawingEffect {

    @Override
    public DynamicAmount drawnCardAmount() {
        return new EventValue();
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.NONE;
    }
}
