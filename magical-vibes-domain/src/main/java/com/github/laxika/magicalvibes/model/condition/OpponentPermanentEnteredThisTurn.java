package com.github.laxika.magicalvibes.model.condition;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * At least {@code minCount} permanents matching the predicate entered the battlefield under an
 * opponent's control this turn.
 */
public record OpponentPermanentEnteredThisTurn(CardPredicate predicate, int minCount) implements Condition {

    @Override
    public String conditionName() {
        return minCount + " or more permanents entered under an opponent's control this turn";
    }

    @Override
    public String conditionNotMetReason() {
        return "not enough permanents entered under an opponent's control this turn";
    }
}
