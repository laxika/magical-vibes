package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/** Reduces an activated ability's generic mana cost by a dynamically evaluated amount. */
public record ReduceActivationCostEffect(DynamicAmount amount) implements ActivationCostModifierEffect {

    @Override
    public boolean reducesGenericCost() {
        return true;
    }
}
