package com.github.laxika.magicalvibes.model.effect;

/**
 * Static replacement effect: if an opponent of this permanent's controller would gain life, that
 * player loses that much life instead. Used by Tainted Remedy.
 *
 * <p>Applied in {@code LifeSupport} after the "can't gain life" prohibitions (a life-gain event that
 * can't happen has nothing to replace) but before life-gain doublers — the affected player chooses
 * the order the replacement effects apply in per CR 616.1, and converting first is always their
 * better choice. Per CR 119.10 a gain of 0 is not a life-gain event, so nothing is replaced.
 */
public record OpponentLifeGainBecomesLifeLossEffect() implements CardEffect {
}
