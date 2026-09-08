package com.github.laxika.magicalvibes.model.effect;

/**
 * Deals damage to each player and planeswalker previously damaged by the source permanent.
 * The source's remembered recipients are battlefield-object state, so the effect is non-targeting
 * and uses only recipients that are still legal destinations when it resolves.
 */
public record DealDamageToPreviouslyDamagedBySourceEffect(int damage) implements CardEffect {
}
