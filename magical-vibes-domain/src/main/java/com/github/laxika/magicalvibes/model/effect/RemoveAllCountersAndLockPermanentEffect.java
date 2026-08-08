package com.github.laxika.magicalvibes.model.effect;

/**
 * "Remove all counters from target creature. It can't have counters put on it for as long as this
 * creature remains on the battlefield." (Suncleanser's first mode.)
 *
 * <p>Removes every counter of every kind — unlike {@link RemoveAllCountersEffect}, which is scoped
 * to one {@code CounterType}. The lock is recorded in
 * {@code GameData.countersLockedPermanentsWhileSourceOnBattlefield} keyed by the source permanent,
 * and read back by {@code GameQueryService.cantHaveCounters}; it ends on its own when the source
 * leaves the battlefield, so nothing has to clean the entry up.</p>
 */
public record RemoveAllCountersAndLockPermanentEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.creature());
    }
}
