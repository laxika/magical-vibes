package com.github.laxika.magicalvibes.model.effect;

/**
 * Schedule every permanent created earlier in this same resolution to be sacrificed at end of
 * combat. Place it in the same slot right after a token-creating effect.
 */
public record SacrificeCreatedPermanentsAtEndOfCombatEffect() implements CardEffect {
}
