package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.EnchantedPermanentPower;

/** Sacrifice the creature equipped by the source, then draw cards equal to its power. */
public record SacrificeEquippedCreatureThenDrawPowerEffect() implements CardDrawingEffect {

    @Override
    public DynamicAmount drawnCardAmount() {
        return new EnchantedPermanentPower();
    }
}
