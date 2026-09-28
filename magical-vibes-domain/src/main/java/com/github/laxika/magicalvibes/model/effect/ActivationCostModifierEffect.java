package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/**
 * A dynamic adjustment to an activated ability's mana cost.
 * The amount is evaluated when the ability is activated, before its costs are paid.
 */
public interface ActivationCostModifierEffect extends CostEffect {

    /** The amount by which the activation cost is adjusted. */
    DynamicAmount amount();

    /** Whether the evaluated amount reduces the activation cost rather than increasing it. */
    boolean reducesGenericCost();

    /**
     * Whether the evaluated amount also adjusts the generic portion of the cost. Most activation
     * cost modifiers do; a colored mana-cost reduction overrides this because it is applied to the
     * complete mana cost separately.
     */
    default boolean modifiesGenericCost() {
        return true;
    }

    /**
     * Optional reduction of the complete mana cost after evaluating {@code amount}. This is used
     * for activation-cost text that removes colored mana symbols as well as generic mana.
     */
    default ManaCost manaCostReduction(int amount) {
        return null;
    }
}
