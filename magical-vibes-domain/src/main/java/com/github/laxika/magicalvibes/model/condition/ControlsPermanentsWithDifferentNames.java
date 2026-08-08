package com.github.laxika.magicalvibes.model.condition;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * The controller controls at least {@code minCount} permanents matching the predicate that all have
 * different names — "if you control four or more Demons with different names" (Liliana's Contract).
 * Permanents whose name is shared with another matching permanent are counted only once.
 */
public record ControlsPermanentsWithDifferentNames(int minCount, PermanentPredicate filter) implements Condition {

    @Override
    public String conditionName() {
        return "controls " + minCount + " or more matching permanents with different names";
    }

    @Override
    public String conditionNotMetReason() {
        return "controller controls fewer than " + minCount + " matching permanents with different names";
    }
}
