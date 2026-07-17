package com.github.laxika.magicalvibes.model.action;

import com.github.laxika.magicalvibes.model.CounterType;

import java.util.UUID;

/**
 * Permanent scheduled to have {@code amount} counters of {@code counterType} removed when combat
 * ends (e.g. Clockwork Beast's "At end of combat, if this creature attacked or blocked this combat,
 * remove a +1/+0 counter from it"). Delaying to end of combat keeps the creature at full power for
 * the combat damage step. The removal-analog of {@link PutCounterOnPermanentAtEndOfCombat}; drained
 * in {@code CombatService.processEndOfCombatCounterRemovals()}.
 */
public record RemoveCounterFromSourceAtEndOfCombat(UUID permanentId, CounterType counterType, int amount)
        implements DelayedAction {
}
