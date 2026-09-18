package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.TotalCountersOnSource;

/**
 * The controller may draw cards equal to the total number of counters on the source permanent.
 * If they do, they lose that same amount of life.
 */
public record DrawAndLoseLifePerCounterOnSourceEffect()
        implements CardDrawingEffect, CombatDamageTriggerContextEffect {

    @Override
    public DynamicAmount drawnCardAmount() {
        return new TotalCountersOnSource();
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.NONE;
    }

    @Override
    public TriggerContext combatDamageTriggerContext() {
        return TriggerContext.DAMAGED_PLAYER;
    }
}
