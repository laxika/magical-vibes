package com.github.laxika.magicalvibes.model.effect;

/**
 * Static marker for a permanent that cannot be targeted by a spell or ability whose legal
 * permanent targets are restricted to Walls. The targeting services inspect this marker directly.
 */
public record WallOnlyTargetingRestrictionEffect() implements CardEffect {
}
