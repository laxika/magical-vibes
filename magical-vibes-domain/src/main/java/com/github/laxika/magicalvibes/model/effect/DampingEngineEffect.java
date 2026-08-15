package com.github.laxika.magicalvibes.model.effect;

/**
 * Static Damping Engine restriction: a player who controls more permanents than every other player
 * cannot play lands or cast artifact, creature, or enchantment spells.
 */
public record DampingEngineEffect() implements CardEffect {
}
