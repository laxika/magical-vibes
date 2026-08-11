package com.github.laxika.magicalvibes.model.effect;

/**
 * Chooses creatures the controller controls at resolution, then uses the power difference of the
 * chosen creatures to draw cards and give those creatures a temporary boost and trample.
 */
public record ChooseTwoCreaturesByPowerDifferenceEffect() implements CardEffect {
}
