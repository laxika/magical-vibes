package com.github.laxika.magicalvibes.model.effect;

/**
 * Wrapper for a Room ability that fires only when the specified door of that Room becomes unlocked.
 */
public record TriggeringRoomDoorConditionalEffect(int doorIndex, CardEffect wrapped)
        implements CardEffect, OptionalTargetEffect {

    @Override
    public boolean hasAbilityResolutionCondition() {
        return wrapped.hasAbilityResolutionCondition();
    }

    @Override
    public TargetSpec targetSpec() {
        return wrapped.targetSpec();
    }
}
