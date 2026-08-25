package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.TargetPower;

/**
 * Sets the source permanent's base power to a dynamically evaluated value until end of turn.
 */
public record SetSelfBasePowerToAmountUntilEndOfTurnEffect(DynamicAmount power) implements CardEffect {

    @Override
    public boolean usesEnteringPermanentReference() {
        return power instanceof TargetPower;
    }
}
