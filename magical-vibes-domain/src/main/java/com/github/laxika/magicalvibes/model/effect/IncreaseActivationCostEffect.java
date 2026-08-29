package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/** Increases an activated ability's generic mana cost by a dynamically evaluated amount. */
public record IncreaseActivationCostEffect(DynamicAmount amount) implements ActivationCostModifierEffect {

    @Override
    public boolean reducesGenericCost() {
        return false;
    }
}
