package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/**
 * Reduces an activated ability's mana cost by {@code reduction} when the evaluated amount is
 * positive. The amount is a gate so conditional reductions can reuse the ordinary dynamic-amount
 * and condition machinery.
 */
public record ReduceActivationCostByManaCostEffect(DynamicAmount amount, ManaCost reduction)
        implements ActivationCostModifierEffect {

    public ReduceActivationCostByManaCostEffect(DynamicAmount amount, String reduction) {
        this(amount, new ManaCost(reduction));
    }

    @Override
    public boolean reducesGenericCost() {
        return true;
    }

    @Override
    public boolean modifiesGenericCost() {
        return false;
    }

    @Override
    public ManaCost manaCostReduction(int amount) {
        return amount > 0 ? reduction : null;
    }
}
