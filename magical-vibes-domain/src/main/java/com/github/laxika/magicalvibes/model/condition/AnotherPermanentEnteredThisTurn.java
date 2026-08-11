package com.github.laxika.magicalvibes.model.condition;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * At least one permanent matching the predicate, other than the source permanent,
 * entered the battlefield under the controller's control this turn.
 */
public record AnotherPermanentEnteredThisTurn(CardPredicate predicate) implements Condition {

    @Override
    public String conditionName() {
        return "another matching permanent entered this turn";
    }

    @Override
    public String conditionNotMetReason() {
        return "no other matching permanent entered the battlefield this turn";
    }
}
