package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/**
 * Deals {@code amount} damage divided evenly, rounded down, among all creatures the targeted
 * player controls — each creature is dealt {@code floor(amount / creatureCount)}. If the target
 * controls no creatures, nothing happens. The player is only the target; no damage is dealt to
 * the player. Used by Dwarven Catapult ({@code amount} is an {@code XValue}).
 *
 * <p>The card restricts the target to an opponent via a card-level
 * {@code PlayerPredicateTargetFilter}; the spec here just declares the structural player target.</p>
 */
public record DealDamageDividedEvenlyAmongCreaturesTargetControlsEffect(DynamicAmount amount)
        implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetCategory.PLAYER);
    }
}
