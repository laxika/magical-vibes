package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * The source permanent gets a temporary boost, or the permanent it is attached to gets that
 * boost when the source is attached.
 */
public record BoostSelfOrEnchantedCreatureUntilEndOfTurnEffect(
        DynamicAmount powerBoost,
        DynamicAmount toughnessBoost
) implements CardEffect {

    public BoostSelfOrEnchantedCreatureUntilEndOfTurnEffect(int powerBoost, int toughnessBoost) {
        this(new Fixed(powerBoost), new Fixed(toughnessBoost));
    }

    @Override
    public TargetSpec targetSpec() {
        return new TargetSpec(null, false, null, true, 1);
    }

    @Override
    public boolean resolvesAgainstAttachedPermanent() {
        return true;
    }
}
