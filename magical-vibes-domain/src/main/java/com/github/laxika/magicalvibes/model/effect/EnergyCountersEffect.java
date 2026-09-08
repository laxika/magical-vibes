package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Changes the controller's energy-counter total. Positive amounts add energy; negative amounts
 * pay energy. The effect is intended to be composed with {@link ConditionalEffect} when payment
 * is optional.
 */
public record EnergyCountersEffect(DynamicAmount amount) implements CombatDamageAmountAwareEffect {

    public EnergyCountersEffect {
        if (amount == null) {
            throw new NullPointerException("amount");
        }
        if (amount instanceof Fixed fixed && fixed.value() == 0) {
            throw new IllegalArgumentException("Energy counter change cannot be zero");
        }
    }

    public EnergyCountersEffect(int amount) {
        this(new Fixed(amount));
    }

    @Override
    public DynamicAmount combatDamageAmount() {
        return amount;
    }
}
