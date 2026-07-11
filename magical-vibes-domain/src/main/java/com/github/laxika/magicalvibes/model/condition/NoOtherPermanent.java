package com.github.laxika.magicalvibes.model.condition;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * The controller controls no permanent matching the predicate other than the
 * source permanent itself.
 */
public record NoOtherPermanent(PermanentPredicate filter) implements Condition {

    @Override
    public String conditionName() {
        return "no other matching permanents";
    }

    @Override
    public String conditionNotMetReason() {
        return "controller controls another matching permanent";
    }
}
