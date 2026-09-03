package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * The source permanent gets +X/+Y. As a one-shot effect (triggered/activated ability) the
 * boost lasts for its configured duration; in the {@code STATIC} slot it is a continuous self bonus.
 * The amounts are {@link DynamicAmount}s, so "gets +1/+0 for each artifact you control"
 * and "gets +2/+2" are the same effect with different amount parameters.
 */
public record BoostSelfEffect(DynamicAmount powerBoost, DynamicAmount toughnessBoost,
                              EffectDuration duration) implements CardEffect {

    public BoostSelfEffect(DynamicAmount powerBoost, DynamicAmount toughnessBoost) {
        this(powerBoost, toughnessBoost, EffectDuration.UNTIL_END_OF_TURN);
    }

    /** Convenience for plain fixed boosts ("gets +2/+2 until end of turn"). */
    public BoostSelfEffect(int powerBoost, int toughnessBoost) {
        this(new Fixed(powerBoost), new Fixed(toughnessBoost), EffectDuration.UNTIL_END_OF_TURN);
    }

    public BoostSelfEffect(int powerBoost, int toughnessBoost, EffectDuration duration) {
        this(new Fixed(powerBoost), new Fixed(toughnessBoost), duration);
    }

    @Override
    public TargetSpec targetSpec() {
        return new TargetSpec(null, false, null, true, 1);
    }
}
