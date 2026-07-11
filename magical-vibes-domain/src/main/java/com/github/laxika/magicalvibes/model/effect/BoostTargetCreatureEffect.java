package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Target creature gets +X/+Y until end of turn. The amounts are {@link DynamicAmount}s, so
 * "gets +3/+3", "gets +X/+X" (X paid), and "gets +1/+1 for each creature you control" are the
 * same effect with different amount parameters. Counting contexts ("you control", "in your
 * graveyard") resolve against the effect's controller, not the target.
 */
public record BoostTargetCreatureEffect(DynamicAmount powerBoost, DynamicAmount toughnessBoost) implements CardEffect {

    /** Convenience for plain fixed boosts ("gets +2/+2 until end of turn"). */
    public BoostTargetCreatureEffect(int powerBoost, int toughnessBoost) {
        this(new Fixed(powerBoost), new Fixed(toughnessBoost));
    }

    @Override
    public boolean canTargetPermanent() {
        return true;
    }
}
