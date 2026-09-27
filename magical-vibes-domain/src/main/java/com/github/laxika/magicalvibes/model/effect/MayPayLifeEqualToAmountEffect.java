package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/** Offers an optional payment of life equal to a dynamic amount before resolving an effect. */
public record MayPayLifeEqualToAmountEffect(DynamicAmount amount, CardEffect wrapped, String prompt)
        implements CardEffect, TriggeringSpellManaValueEffect {

    public MayPayLifeEqualToAmountEffect {
        if (amount == null) {
            throw new IllegalArgumentException("MayPayLifeEqualToAmountEffect requires an amount");
        }
        if (wrapped == null) {
            throw new IllegalArgumentException("MayPayLifeEqualToAmountEffect requires a wrapped effect");
        }
        if (prompt == null || prompt.isBlank()) {
            throw new IllegalArgumentException("MayPayLifeEqualToAmountEffect requires a prompt");
        }
    }

    @Override
    public TargetSpec targetSpec() {
        return wrapped.targetSpec();
    }
}
