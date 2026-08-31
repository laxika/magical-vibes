package com.github.laxika.magicalvibes.model.effect;

/**
 * Each opponent discards one card if able. Each opponent who cannot discard this way loses life.
 */
public record EachOpponentDiscardsOrLosesLifeEffect(int lifeLoss) implements CardEffect {
}
