package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.TargetPower;

/**
 * The controller gains life equal to the effective power of a single target creature (never
 * negative). Unlike {@code GainLifeEffect(new TargetPower())} — whose target is established by a
 * co-resolving targeted effect (e.g. Chastise's destroy) — this effect declares its own creature
 * {@link TargetSpec}, so it can stand alone as a targeted triggered ability (Wall of Reverence).
 */
public record GainLifeEqualToTargetCreaturePowerEffect() implements LifeGainEffect {

    @Override
    public DynamicAmount lifeGainAmount() {
        return new TargetPower();
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetCategory.CREATURE);
    }
}
