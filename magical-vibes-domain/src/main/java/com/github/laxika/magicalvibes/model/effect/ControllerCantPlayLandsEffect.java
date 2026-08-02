package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: the source's controller can't play lands. Used by Aggressive Mining (M15).
 * Enforced in {@code CastingPermissionService#isLandPlayRestricted}.
 */
public record ControllerCantPlayLandsEffect() implements CardEffect {
}
