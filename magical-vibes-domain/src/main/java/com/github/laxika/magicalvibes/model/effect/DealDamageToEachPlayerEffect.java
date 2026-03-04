package com.github.laxika.magicalvibes.model.effect;

/**
 * Deals a fixed amount of damage to each player (not creatures).
 * Used by modal spells like Slagstorm's second mode: "deals 3 damage to each player."
 */
public record DealDamageToEachPlayerEffect(int damage) implements CardEffect {
}
