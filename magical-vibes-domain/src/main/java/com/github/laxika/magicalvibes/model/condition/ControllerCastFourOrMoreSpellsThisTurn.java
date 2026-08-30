package com.github.laxika.magicalvibes.model.condition;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** At least four spells matching the predicate were cast by the controller this turn. */
public record ControllerCastFourOrMoreSpellsThisTurn(CardPredicate filter) implements Condition {

    @Override
    public String conditionName() {
        return "you cast four or more matching spells this turn";
    }

    @Override
    public String conditionNotMetReason() {
        return "you haven't cast four or more matching spells this turn";
    }
}
