package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: the source controller's second spell cast each turn costs the given amount less
 * generic mana.
 */
public record ReduceSecondSpellCastCostEffect(int amount) implements CardEffect {
}
