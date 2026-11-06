package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.EventValue;

/**
 * Mills cards from the controller's library, then may put a Saga card and a land card milled this
 * way onto the battlefield.
 */
public record MillControllerAndMayPutMilledSagaAndLandOntoBattlefieldEffect(DynamicAmount count)
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
