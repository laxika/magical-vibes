package com.github.laxika.magicalvibes.model.effect;

/**
 * "Deals N damage divided as you choose among up to M targets (creatures and/or players)."
 *
 * <p>Damage assignments (target UUID → amount) are supplied via
 * {@code GameData.pendingETBDamageAssignments}. Targeting is handled through
 * that map, not the standard targeting system, so {@code canTargetPermanent()}
 * and {@code canTargetPlayer()} intentionally remain {@code false}.
 *
 * <p>Used by cards like Inferno Titan (3 damage divided among 1-3 targets on ETB/attack).
 */
public record DealDividedDamageToAnyTargetsEffect(int totalDamage, int maxTargets) implements CardEffect {
}
