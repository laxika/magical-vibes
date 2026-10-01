package com.github.laxika.magicalvibes.model.condition;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** The controller's library contains no card matching the predicate. */
public record NoCardsInLibraryMatching(CardPredicate filter) implements Condition {

    @Override
    public String conditionName() {
        return "no matching cards in library";
    }

    @Override
    public String conditionNotMetReason() {
        return "the library contains a matching card";
    }

    @Override
    public boolean isEtbTriggerGate() {
        return true;
    }
}
