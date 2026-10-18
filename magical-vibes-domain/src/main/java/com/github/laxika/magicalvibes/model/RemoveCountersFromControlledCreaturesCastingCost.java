package com.github.laxika.magicalvibes.model;

/**
 * An additional cost that removes the specified counters from among creatures controlled by the
 * player casting the spell. A payment may remove multiple counters from one creature or split them
 * among several creatures. A null {@code counterType} means counters of any kind may be removed
 * (Quilled Greatwurm); use {@link #anyKind(int)} to build that form.
 */
public record RemoveCountersFromControlledCreaturesCastingCost(int count, CounterType counterType)
        implements CastingCost {

    public static RemoveCountersFromControlledCreaturesCastingCost anyKind(int count) {
        return new RemoveCountersFromControlledCreaturesCastingCost(count, null);
    }
}
