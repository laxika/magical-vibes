package com.github.laxika.magicalvibes.model.effect;

/**
 * Each player discards one card, then each player who did not discard a creature card this way
 * loses life.
 */
public record EachPlayerDiscardsCreatureOrLosesLifeEffect(int lifeLoss) implements CardEffect {
}
