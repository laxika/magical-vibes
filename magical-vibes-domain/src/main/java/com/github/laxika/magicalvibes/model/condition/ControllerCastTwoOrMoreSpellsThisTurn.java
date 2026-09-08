package com.github.laxika.magicalvibes.model.condition;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** At least two spells matching the predicate were cast by the controller this turn. */
public record ControllerCastTwoOrMoreSpellsThisTurn(CardPredicate filter) implements Condition {

    @Override
    public String conditionName() {
        return "you cast two or more matching spells this turn";
    }

    @Override
    public String conditionNotMetReason() {
        return "you haven't cast two or more matching spells this turn";
    }

    @Override
    public boolean isEtbTriggerGate() {
        return true;
    }
}
