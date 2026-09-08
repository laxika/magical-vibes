package com.github.laxika.magicalvibes.model.effect;

/**
 * Each player may discard a card. Each player who does not discard loses life instead.
 */
public record EachPlayerMayDiscardOrLoseLifeEffect(int lifeLoss) implements CardEffect {
}
