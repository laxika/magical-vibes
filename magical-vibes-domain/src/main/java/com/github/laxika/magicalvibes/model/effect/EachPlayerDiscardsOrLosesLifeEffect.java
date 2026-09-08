package com.github.laxika.magicalvibes.model.effect;

/**
 * Each player discards one card if able. Each opponent who cannot discard this way loses life.
 */
public record EachPlayerDiscardsOrLosesLifeEffect(int lifeLoss) implements CardEffect {
}
