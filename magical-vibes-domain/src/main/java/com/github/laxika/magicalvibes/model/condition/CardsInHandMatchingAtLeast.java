package com.github.laxika.magicalvibes.model.condition;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * The controller has at least {@code threshold} cards in hand matching the predicate.
 */
public record CardsInHandMatchingAtLeast(int threshold, CardPredicate filter) implements Condition {

    @Override
    public String conditionName() {
        return "matching cards in hand (" + threshold + "+)";
    }

    @Override
    public String conditionNotMetReason() {
        return "fewer than " + threshold + " matching cards in hand";
    }
}
