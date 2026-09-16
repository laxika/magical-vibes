package com.github.laxika.magicalvibes.model.effect;

/**
 * Reduces the controller's spell costs by one generic mana for each of the source permanent's
 * chosen colors present in the spell.
 */
public record ReduceCastCostForEachChosenColorEffect() implements CardEffect {
}
