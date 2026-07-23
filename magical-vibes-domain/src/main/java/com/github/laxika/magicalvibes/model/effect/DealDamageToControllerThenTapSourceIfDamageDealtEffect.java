package com.github.laxika.magicalvibes.model.effect;

/**
 * Deals {@code damage} noncombat damage to the source permanent's controller, then taps the source
 * only if the damage actually landed (controller's life dropped). Fallback-only: interpreted by
 * {@code DestructionSupport.resolveForcedCostElseEffects}, not the effect dispatch registry.
 *
 * <p>Used as a {@link ForcedCostOrElseEffect} else-effect for "this creature deals N damage to you
 * unless you [cost]. If this creature deals damage to you this way, tap it." (Minion of Leshrac).
 * Redirected or prevented damage leaves the source untapped.
 */
public record DealDamageToControllerThenTapSourceIfDamageDealtEffect(int damage) implements CardEffect {
}
