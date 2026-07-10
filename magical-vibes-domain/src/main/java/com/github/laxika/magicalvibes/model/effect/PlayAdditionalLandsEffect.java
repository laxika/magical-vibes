package com.github.laxika.magicalvibes.model.effect;

/**
 * Grants the controller permission to play up to {@code count} additional lands this turn,
 * on top of the normal one-per-turn allowance. Used by Summer Bloom.
 */
public record PlayAdditionalLandsEffect(int count) implements CardEffect {
}
