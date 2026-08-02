package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/**
 * The controller gains life equal to {@code amount} evaluated against a single target creature
 * (never negative). Unlike {@code GainLifeEffect(new TargetPower())} — whose target is established
 * by a co-resolving targeted effect (e.g. Chastise's destroy) — this effect declares its own
 * creature {@link TargetSpec}, so it can stand alone as the targeted effect of a spell or of a
 * triggered ability.
 *
 * <p>Wall of Reverence passes {@code TargetPower()}; Predator's Rapport passes
 * {@code Sum(TargetPower(), TargetToughness())}.
 */
public record GainLifeEqualToTargetCreatureStatEffect(DynamicAmount amount)
        implements LifeGainEffect {

    @Override
    public DynamicAmount lifeGainAmount() {
        return amount;
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetCategory.CREATURE);
    }
}
