package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect that reduces the generic mana cost of spells cast from graveyards.
 */
public record ReduceGraveyardSpellCastCostEffect(int amount) implements CardEffect {
}
