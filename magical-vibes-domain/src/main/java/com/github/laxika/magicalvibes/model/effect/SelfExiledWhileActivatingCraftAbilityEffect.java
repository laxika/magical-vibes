package com.github.laxika.magicalvibes.model.effect;

/**
 * Trigger descriptor for a wrapped effect that fires when the source permanent is exiled from the
 * battlefield as part of activating a craft ability.
 */
public record SelfExiledWhileActivatingCraftAbilityEffect(CardEffect wrapped) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return wrapped.targetSpec();
    }
}
