package com.github.laxika.magicalvibes.model.effect;

/**
 * Reduces the cost of a spell cast by this effect's controller when the spell targets the player
 * enchanted by the permanent carrying this effect.
 */
public record ReduceOwnCastCostIfTargetingEnchantedPlayerEffect(int amount) implements CardEffect {
}
