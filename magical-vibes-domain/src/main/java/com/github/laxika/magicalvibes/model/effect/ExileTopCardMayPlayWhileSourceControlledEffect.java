package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles the top card of the controller's library and lets that player play it while they
 * control the source permanent.
 */
public record ExileTopCardMayPlayWhileSourceControlledEffect()
        implements CombatDamageTriggerContextEffect {

    @Override
    public TriggerContext combatDamageTriggerContext() {
        return TriggerContext.SOURCE_SELF;
    }
}
