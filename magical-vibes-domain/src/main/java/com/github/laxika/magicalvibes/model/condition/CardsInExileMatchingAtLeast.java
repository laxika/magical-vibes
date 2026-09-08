package com.github.laxika.magicalvibes.model.condition;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * The controller owns at least {@code threshold} nontoken cards in exile matching the predicate.
 */
public record CardsInExileMatchingAtLeast(int threshold, CardPredicate filter) implements Condition {

    @Override
    public String conditionName() {
        return "matching cards in exile (" + threshold + "+)";
    }

    @Override
    public String conditionNotMetReason() {
        return "fewer than " + threshold + " matching cards in exile";
    }
}
