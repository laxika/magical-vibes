package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: each opponent's maximum hand size is reduced by the given amount.
 * Checked during cleanup when calculating discard requirements.
 */
public record ReduceOpponentMaxHandSizeEffect(int reduction) implements CardEffect {
}
