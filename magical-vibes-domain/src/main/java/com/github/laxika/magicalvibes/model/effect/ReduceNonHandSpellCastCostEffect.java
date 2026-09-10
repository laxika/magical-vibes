package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect that reduces the generic mana cost of spells cast from any zone other than hand.
 */
public record ReduceNonHandSpellCastCostEffect(int amount) implements CardEffect {
}
