package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/**
 * Deals damage to each of multiple targets (creatures and/or players). The amount is a
 * {@link DynamicAmount} evaluated once at resolution and each target receives the full
 * amount (not divided). Uses {@code entry.getTargetIds()} for targets.
 * Used by Jaya's Immolating Inferno ("deals X damage to each of up to three targets").
 */
public record DealDamageToEachTargetEffect(DynamicAmount damage) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetCategory.PLAYER_OR_PERMANENT);
    }
}
