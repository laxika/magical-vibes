package com.github.laxika.magicalvibes.model.action;

import com.github.laxika.magicalvibes.model.CounterType;

import java.util.UUID;

/**
 * Permanent scheduled to receive {@code amount} counters of {@code counterType} when combat ends
 * (e.g. Greater Werewolf's "At end of combat, put a -0/-2 counter on each creature blocking or
 * blocked by this creature"). Unlike {@link PutMinusOneCounterAtEndOfCombat} (which always targets
 * the source with -1/-1 counters), this carries an arbitrary target permanent and counter type.
 * Drained in {@code CombatService.processEndOfCombatOpponentCounters()}.
 */
public record PutCounterOnPermanentAtEndOfCombat(UUID permanentId, CounterType counterType, int amount)
        implements DelayedAction {
}
