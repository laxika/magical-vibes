package com.github.laxika.magicalvibes.model.condition;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** The source permanent has at least {@code threshold} exiled cards matching the predicate. */
public record SourceExiledCardsMatchingAtLeast(int threshold, CardPredicate filter) implements Condition {

    @Override
    public String conditionName() {
        return "matching cards exiled with source (" + threshold + "+)";
    }

    @Override
    public String conditionNotMetReason() {
        return "fewer than " + threshold + " matching cards exiled with source";
    }
}
