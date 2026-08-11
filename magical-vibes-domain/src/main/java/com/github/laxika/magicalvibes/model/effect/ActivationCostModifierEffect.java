package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/**
 * A dynamic adjustment to the generic mana portion of an activated ability's mana cost.
 * The amount is evaluated when the ability is activated, before its costs are paid.
 */
public interface ActivationCostModifierEffect extends CostEffect {

    /** The amount by which the activation cost is adjusted. */
    DynamicAmount amount();

    /** Whether the evaluated amount reduces the activation cost rather than increasing it. */
    boolean reducesGenericCost();
}
