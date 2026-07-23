package com.github.laxika.magicalvibes.model.effect;

/**
 * STATIC replacement (Reality Twist): if tapped for mana, Plains produce {R}, Swamps produce {G},
 * Mountains produce {W}, and Forests produce {B} instead of any other type. Islands are unchanged.
 * Applied in mana-ability resolution via {@code GameQueryService.twistedLandManaColors}.
 */
public record RealityTwistManaEffect() implements TwistBasicLandManaColorsEffect {
}
