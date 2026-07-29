package com.github.laxika.magicalvibes.model.condition;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * At least one opponent of the controller has cast a spell matching the predicate this turn.
 * A countered or otherwise fizzled spell still counts as cast. Used by Lure of Prey.
 */
public record OpponentCastSpellThisTurn(CardPredicate filter) implements Condition {

    @Override
    public String conditionName() {
        return "an opponent cast a matching spell this turn";
    }

    @Override
    public String conditionNotMetReason() {
        return "no opponent cast a matching spell this turn";
    }
}
