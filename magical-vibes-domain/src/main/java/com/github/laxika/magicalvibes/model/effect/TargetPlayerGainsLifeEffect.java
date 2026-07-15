package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Target player gains {@code amount} life. The amount is a {@link DynamicAmount} evaluated at
 * resolution (fixed number, X paid, …) — e.g. Stream of Life's "target player gains X life".
 */
public record TargetPlayerGainsLifeEffect(DynamicAmount amount) implements CardEffect {

    public TargetPlayerGainsLifeEffect(int amount) {
        this(new Fixed(amount));
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetCategory.PLAYER);
    }
}
