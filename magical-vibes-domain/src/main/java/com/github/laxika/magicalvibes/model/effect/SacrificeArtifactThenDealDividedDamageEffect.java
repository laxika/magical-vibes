package com.github.laxika.magicalvibes.model.effect;

/**
 * "Sacrifice an artifact. If you do, [source] deals N damage divided
 * as you choose among any number of targets."
 *
 * <p>Wrap in {@link MayEffect} when the sacrifice is optional ("you may").
 * The total damage is parameterized for reusability. Damage assignments
 * (target → amount) are supplied at cast time via
 * {@code GameData.pendingETBDamageAssignments}.
 *
 * <p>Targeting is handled via {@code pendingETBDamageAssignments}, not the
 * standard targeting system, so {@code canTargetPermanent()} and
 * {@code canTargetPlayer()} intentionally remain {@code false}.
 */
public record SacrificeArtifactThenDealDividedDamageEffect(int totalDamage) implements CardEffect {
}
