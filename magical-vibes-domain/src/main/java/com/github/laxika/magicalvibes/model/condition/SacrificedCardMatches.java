package com.github.laxika.magicalvibes.model.condition;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** The permanent sacrificed as an additional cast cost had the given card characteristics. */
public record SacrificedCardMatches(CardPredicate filter, String description) implements Condition {

    @Override
    public String conditionName() {
        return "sacrificed permanent was " + description;
    }

    @Override
    public String conditionNotMetReason() {
        return "the sacrificed permanent was not " + description;
    }
}
