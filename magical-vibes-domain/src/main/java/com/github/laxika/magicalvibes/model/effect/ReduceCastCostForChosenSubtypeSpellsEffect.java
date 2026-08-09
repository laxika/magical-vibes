package com.github.laxika.magicalvibes.model.effect;

/**
 * Reduces the generic mana cost of the source controller's creature spells that have the
 * source permanent's chosen creature subtype.
 */
public record ReduceCastCostForChosenSubtypeSpellsEffect(int amount) implements CardEffect {
}
