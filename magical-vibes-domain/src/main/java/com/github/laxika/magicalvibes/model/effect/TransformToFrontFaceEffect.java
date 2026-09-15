package com.github.laxika.magicalvibes.model.effect;

/**
 * Transforms the source permanent to its front face when it is still on the battlefield and
 * transformed. Used by combat-damage triggers on converted back faces.
 */
public record TransformToFrontFaceEffect() implements CombatDamageTriggerContextEffect {

    @Override
    public TriggerContext combatDamageTriggerContext() {
        return TriggerContext.SOURCE_SELF;
    }
}
