package com.github.laxika.magicalvibes.model.effect;

/**
 * Search library for a creature card with mana value exactly equal to xValue + mvOffset,
 * put it onto the battlefield, then shuffle.
 * Used by Birthing Pod (mvOffset = 1: MV = sacrificed creature's MV + 1).
 */
public record SearchLibraryForCreatureWithExactMVToBattlefieldEffect(int mvOffset) implements CardEffect {
}
