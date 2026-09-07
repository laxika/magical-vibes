package com.github.laxika.magicalvibes.model.condition;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/** At least {@code minCount} matching permanents exist across all battlefields. */
public record AnyPlayerControlsPermanentCount(int minCount, PermanentPredicate filter, boolean excludeSource)
        implements Condition {

    public AnyPlayerControlsPermanentCount(int minCount, PermanentPredicate filter) {
        this(minCount, filter, false);
    }

    @Override
    public String conditionName() {
        return minCount + " or more matching permanents on the battlefield";
    }

    @Override
    public String conditionNotMetReason() {
        return "fewer than " + minCount + " matching permanents on the battlefield";
    }

    @Override
    public boolean isEtbTriggerGate() {
        return true;
    }
}
