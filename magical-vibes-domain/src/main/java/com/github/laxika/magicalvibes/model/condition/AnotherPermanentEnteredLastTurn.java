package com.github.laxika.magicalvibes.model.condition;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** At least one matching permanent other than the source entered under the controller's control last turn. */
public record AnotherPermanentEnteredLastTurn(CardPredicate predicate) implements Condition {

    @Override
    public String conditionName() {
        return "another matching permanent entered last turn";
    }

    @Override
    public String conditionNotMetReason() {
        return "no other matching permanent entered the battlefield last turn";
    }
}
