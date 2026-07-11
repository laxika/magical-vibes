package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * The source permanent deals damage to itself. The amount is a {@link DynamicAmount}
 * evaluated at resolution (fixed number, counters on the source, cards in graveyard, …).
 * Used alongside another damage effect for cards like Sunflare Shaman ("deals X damage to
 * any target and X damage to itself").
 *
 * @param damage the amount of damage the source deals to itself
 */
public record DealDamageToSourceEffect(DynamicAmount damage) implements CardEffect {

    public DealDamageToSourceEffect(int damage) {
        this(new Fixed(damage));
    }
}
