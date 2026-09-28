package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/** Chooses one suspended card the controller owns and removes the evaluated number of time counters from it. */
public record ChooseSuspendedCardAndRemoveTimeCountersEffect(DynamicAmount amount)
        implements CombatDamageAmountAwareEffect {

    @Override
    public DynamicAmount combatDamageAmount() {
        return amount;
    }
}
