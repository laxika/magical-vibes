package com.github.laxika.magicalvibes.model.effect;

/**
 * Deals a fixed amount of damage to each opponent (not creatures, not the controller).
 * Used by cards like Cabal Paladin: "deals 2 damage to each opponent."
 */
public record DealDamageToEachOpponentEffect(int damage) implements CardEffect {
}
