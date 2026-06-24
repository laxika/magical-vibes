package com.github.laxika.magicalvibes.model.effect;

/**
 * Deals damage to the player or planeswalker attacked by the creature that caused
 * an attack trigger.
 */
public record DealDamageToAttackedTargetEffect(int damage) implements CardEffect {
}
