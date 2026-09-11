package com.github.laxika.magicalvibes.model.condition;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * The controller owns a matching nontoken card in hand, graveyard, face-up exile, and on the
 * battlefield.
 */
public record OwnsCardInAllZones(CardPredicate filter) implements Condition {

    @Override
    public String conditionName() {
        return "a matching card in hand, graveyard, exile, and on the battlefield";
    }

    @Override
    public String conditionNotMetReason() {
        return "no matching card in all four zones";
    }
}
