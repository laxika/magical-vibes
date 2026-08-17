package com.github.laxika.magicalvibes.model.condition;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** At least three spells matching the predicate were cast by the controller this turn. */
public record ControllerCastThreeOrMoreSpellsThisTurn(CardPredicate filter) implements Condition {

    @Override
    public String conditionName() {
        return "you cast three or more matching spells this turn";
    }

    @Override
    public String conditionNotMetReason() {
        return "you haven't cast three or more matching spells this turn";
    }
}
