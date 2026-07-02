package com.github.laxika.magicalvibes.model.condition;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * The controller has cast another spell matching the predicate this turn
 * (excluding the resolving spell itself).
 */
public record ControllerCastAnotherSpellThisTurn(CardPredicate filter) implements Condition {

    @Override
    public String conditionName() {
        return "another matching spell cast this turn";
    }

    @Override
    public String conditionNotMetReason() {
        return "you haven't cast another matching spell this turn";
    }
}
