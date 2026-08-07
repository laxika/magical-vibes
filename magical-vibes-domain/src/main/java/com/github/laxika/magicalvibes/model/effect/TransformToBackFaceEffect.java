package com.github.laxika.magicalvibes.model.effect;

/**
 * Transforms the source permanent to its back face if it is still on the battlefield and not
 * already transformed. No-ops otherwise (Archangel Avacyn delayed trigger: multiple deaths queue
 * multiple delayed abilities, but only the first that resolves while still front-face transforms).
 *
 * <p>As a combat-damage trigger it needs the damage-dealing permanent bound as the stack entry's
 * source, since that permanent is the one that transforms (Akki Lavarunner).
 */
public record TransformToBackFaceEffect() implements CombatDamageTriggerContextEffect {

    @Override
    public TriggerContext combatDamageTriggerContext() {
        return TriggerContext.SOURCE_SELF;
    }
}
