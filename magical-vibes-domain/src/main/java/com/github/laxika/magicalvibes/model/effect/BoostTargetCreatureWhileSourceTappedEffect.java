package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * "Target creature gets +power/+toughness for as long as the source permanent remains tapped."
 * (Tawnos's Weaponry). Unlike {@link BoostTargetCreatureEffect} (which wears off at end of turn),
 * the handler records this as a {@code WHILE_SOURCE_TAPPED} floating continuous effect keyed to the
 * source permanent: the additive +P/+T is read by the layered pass for as long as the source stays
 * tapped, and the effect is expired the moment the source becomes untapped or leaves the battlefield
 * (CR 611.2b — it does not resume if the source is tapped again).
 *
 * <p>The wrapped floating effect reuses {@link BuffTargetCreatureIndefinitelyEffect}, which the CR
 * 613 layer reader already applies in sublayer 7c; only the floating effect's duration/source differ.
 */
public record BoostTargetCreatureWhileSourceTappedEffect(int power, int toughness) implements CreatureBoostEffect {

    @Override
    public DynamicAmount powerBoost() {
        return new Fixed(power);
    }

    @Override
    public DynamicAmount toughnessBoost() {
        return new Fixed(toughness);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetCategory.CREATURE);
    }
}
