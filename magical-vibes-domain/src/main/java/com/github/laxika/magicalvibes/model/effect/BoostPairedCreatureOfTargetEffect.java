package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Soulbond rider for a targeted pump: "If it's paired with a creature, that creature also gets
 * +X/+Y until end of turn" (Joint Assault). Pair it with a {@link BoostTargetCreatureEffect} in
 * the same target group — this effect boosts the soulbond partner of the target, never the target
 * itself, and does nothing when the target is unpaired or has already left the battlefield.
 *
 * <p>The amounts are {@link DynamicAmount}s evaluated against the effect's controller, exactly as
 * in {@link BoostTargetCreatureEffect}.
 */
public record BoostPairedCreatureOfTargetEffect(DynamicAmount powerBoost, DynamicAmount toughnessBoost)
        implements CreatureBoostEffect {

    /** Convenience for plain fixed boosts ("that creature also gets +2/+2 until end of turn"). */
    public BoostPairedCreatureOfTargetEffect(int powerBoost, int toughnessBoost) {
        this(new Fixed(powerBoost), new Fixed(toughnessBoost));
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature(), null);
    }
}
