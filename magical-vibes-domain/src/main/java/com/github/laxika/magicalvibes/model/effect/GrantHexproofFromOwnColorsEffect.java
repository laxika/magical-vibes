package com.github.laxika.magicalvibes.model.effect;

/**
 * Each other creature controlled by this effect's source has hexproof from each of its colors.
 * The protected colors are evaluated from each affected creature's current characteristics.
 */
public record GrantHexproofFromOwnColorsEffect() implements CardEffect {
}
