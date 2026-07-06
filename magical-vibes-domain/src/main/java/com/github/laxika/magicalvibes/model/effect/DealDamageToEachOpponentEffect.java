package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Deals damage to each opponent (not creatures, not the controller). The amount is a
 * {@link DynamicAmount} evaluated once at resolution — the same value is dealt to every
 * opponent (e.g. Cabal Paladin's fixed 2, Hallar's +1/+1 counter count).
 */
public record DealDamageToEachOpponentEffect(DynamicAmount damage) implements CardEffect {

    public DealDamageToEachOpponentEffect(int damage) {
        this(new Fixed(damage));
    }
}
