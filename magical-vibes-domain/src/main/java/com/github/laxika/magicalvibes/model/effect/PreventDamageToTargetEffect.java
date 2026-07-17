package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Prevent the next {@code amount} damage that would be dealt to any target this turn. The amount is a
 * {@link DynamicAmount} evaluated at resolution (fixed number, X paid, …) — e.g. Alabaster Potion's
 * "prevent the next X damage that would be dealt to any target this turn".
 */
public record PreventDamageToTargetEffect(DynamicAmount amount) implements CardEffect {

    public PreventDamageToTargetEffect(int amount) {
        this(new Fixed(amount));
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetCategory.ANY_TARGET);
    }
}
