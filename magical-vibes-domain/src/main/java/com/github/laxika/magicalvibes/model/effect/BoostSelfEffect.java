package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * The source permanent gets +X/+Y. As a one-shot effect (triggered/activated ability) the
 * boost lasts until end of turn; in the {@code STATIC} slot it is a continuous self bonus.
 * The amounts are {@link DynamicAmount}s, so "gets +1/+0 for each artifact you control"
 * and "gets +2/+2" are the same effect with different amount parameters.
 */
public record BoostSelfEffect(DynamicAmount powerBoost, DynamicAmount toughnessBoost) implements CardEffect {

    /** Convenience for plain fixed boosts ("gets +2/+2 until end of turn"). */
    public BoostSelfEffect(int powerBoost, int toughnessBoost) {
        this(new Fixed(powerBoost), new Fixed(toughnessBoost));
    }

    @Override
    public boolean isSelfTargeting() { return true; }
}
