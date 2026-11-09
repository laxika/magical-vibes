package com.github.laxika.magicalvibes.model.effect;

/**
 * "Sacrifice an artifact. If you do, [source] deals N damage divided
 * as you choose among any number of targets."
 *
 * <p>Wrap in {@link MayEffect} when the sacrifice is optional ("you may").
 * The total damage is parameterized for reusability. As a triggered ability goes on the stack, its
 * controller announces zero or more targets among any targets and divides the damage among them
 * ({@code StackedDividedDamageTriggerService}, CR 601.2d, 603.3d); the stack entry keeps the division.
 * The sacrifice is chosen as the ability resolves; if every target has become illegal, the ability
 * doesn't resolve and nothing is sacrificed (CR 608.2b).
 *
 * <p>The targets are announced by that service rather than the standard targeting system, so the
 * effect declares no target spec.
 */
public record SacrificeArtifactThenDealDividedDamageEffect(int totalDamage) implements CardEffect {
}
