package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: spells and abilities controlled by this permanent's controller can't destroy,
 * exile, target, or cause that controller to sacrifice this permanent.
 */
public record OwnEffectsCantAffectSourceEffect() implements CardEffect {
}
