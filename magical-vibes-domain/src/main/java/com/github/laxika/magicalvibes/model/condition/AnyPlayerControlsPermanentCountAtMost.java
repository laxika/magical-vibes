package com.github.laxika.magicalvibes.model.condition;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/** At most {@code maxCount} permanents matching the predicate exist across all battlefields. */
public record AnyPlayerControlsPermanentCountAtMost(int maxCount, PermanentPredicate filter) implements Condition {

    @Override
    public String conditionName() {
        return maxCount + " or fewer matching permanents on the battlefield";
    }

    @Override
    public String conditionNotMetReason() {
        return "more than " + maxCount + " matching permanents on the battlefield";
    }
}
