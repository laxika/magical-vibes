package com.github.laxika.magicalvibes.model.condition;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * At least {@code minCount} permanents matching the predicate entered the battlefield
 * under the controller's control this turn.
 */
public record PermanentEnteredThisTurn(CardPredicate predicate, int minCount) implements Condition {

    @Override
    public String conditionName() {
        return "permanent entered this turn";
    }

    @Override
    public String conditionNotMetReason() {
        return "not enough permanents entered the battlefield this turn";
    }
}
