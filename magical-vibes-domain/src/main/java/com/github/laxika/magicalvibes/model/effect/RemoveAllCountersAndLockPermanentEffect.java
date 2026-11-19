package com.github.laxika.magicalvibes.model.effect;

/**
 * "Remove all counters from target creature. It can't have counters put on it for as long as this
 * creature remains on the battlefield." (Suncleanser's first mode.)
 *
 * <p>Removes every counter of every kind — unlike {@link RemoveAllCountersEffect}, which is scoped
 * to one {@code CounterType}. The lock is this effect as a floating effect on the target lasting
 * {@link Duration#WHILE_SOURCE_REMAINS}, read back by {@code GameQueryService.cantHaveCounters}.</p>
 */
public record RemoveAllCountersAndLockPermanentEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.creature());
    }
}
