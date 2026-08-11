package com.github.laxika.magicalvibes.model.effect;

/**
 * STATIC replacement: a basic land controlled by the static effect's controller produces mana of
 * a color chosen by that controller instead of its normal mana type.
 */
public record ReplaceBasicLandManaWithAnyColorEffect() implements BasicLandManaProducesAnyColorEffect {
}
