package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Scaled;

/**
 * Increases the generic mana portion of an activated ability's cost by {increasePerCounter} for each
 * counter of {counterType} on the source permanent. Carried in the ability's effect list as a
 * {@link CostEffect}, so it is stripped from the resolved effects and only consulted when
 * computing/paying the activation cost. The count is read when the ability is activated, so counters
 * the ability itself adds do not raise its own cost (CR 601.2f as applied to abilities).
 *
 * <p>Mirror image of {@link ReduceActivationCostPerCounterEffect}. Used by Chromatic Armor
 * ("{X}: Put a sleight counter on this Aura and choose a color. X is the number of sleight counters
 * on this Aura.") with a printed cost of {0}.
 */
public record IncreaseActivationCostPerCounterEffect(CounterType counterType, int increasePerCounter)
        implements ActivationCostModifierEffect {

    @Override
    public DynamicAmount amount() {
        return new Scaled(new CountersOnSource(counterType), increasePerCounter);
    }

    @Override
    public boolean reducesGenericCost() {
        return false;
    }
}
