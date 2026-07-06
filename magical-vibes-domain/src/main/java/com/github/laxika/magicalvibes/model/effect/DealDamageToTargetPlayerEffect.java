package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Deals damage to target player. The amount is a {@link DynamicAmount} evaluated at
 * resolution (fixed number, cards in the controller's graveyard, …).
 */
public record DealDamageToTargetPlayerEffect(DynamicAmount damage) implements CardEffect {

    public DealDamageToTargetPlayerEffect(int damage) {
        this(new Fixed(damage));
    }

    @Override public boolean canTargetPlayer() { return true; }
}
