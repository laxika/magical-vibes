package com.github.laxika.magicalvibes.model.effect;

/**
 * Destroys the source permanent and, only if it is actually put into a graveyard as a result,
 * deals {@code damage} noncombat damage to that permanent's controller. The self analog of
 * {@link DestroyTargetPermanentAndDamageControllerIfDestroyedEffect}.
 *
 * <p>Used as a {@link ForcedCostOrElseEffect} fallback for "destroy this creature unless you pay
 * {cost}. If this creature is destroyed this way, it deals N damage to you." (Cosmic Horror).
 * Regeneration and indestructible are respected, so a saved source deals no damage.
 */
public record DestroySourceAndDamageControllerIfDestroyedEffect(int damage) implements CardEffect {
}
