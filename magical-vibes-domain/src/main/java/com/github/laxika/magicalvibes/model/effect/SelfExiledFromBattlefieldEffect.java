package com.github.laxika.magicalvibes.model.effect;

/**
 * Trigger descriptor for an effect that fires only when its source permanent is put into exile
 * from the battlefield.
 */
public record SelfExiledFromBattlefieldEffect(CardEffect wrapped) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return wrapped.targetSpec();
    }
}
