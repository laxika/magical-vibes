package com.github.laxika.magicalvibes.model.condition;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/** At least {@code minCount} permanents matching the predicate exist across all battlefields. */
public record AnyPlayerControlsPermanentCount(int minCount, PermanentPredicate filter) implements Condition {

    @Override
    public String conditionName() {
        return minCount + " or more matching permanents on the battlefield";
    }

    @Override
    public String conditionNotMetReason() {
        return "fewer than " + minCount + " matching permanents on the battlefield";
    }
}
