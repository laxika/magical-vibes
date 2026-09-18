package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect preventing players from paying life as a spell cost or as a non-mana ability
 * cost.
 */
public record PlayersCantPayLifeEffect() implements CardEffect {
}
