package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.EventValue;

/** Mills the controller's library, then may put any number of milled lands onto the battlefield tapped. */
public record MillControllerAndMayPutMilledLandsOntoBattlefieldEffect(DynamicAmount count)
        implements CombatDamageTriggerContextEffect {

    @Override
    public TriggerContext combatDamageTriggerContext() {
        return TriggerContext.SOURCE_SELF;
    }

    @Override
    public boolean referencesEventValue() {
        return count instanceof EventValue;
    }
}
