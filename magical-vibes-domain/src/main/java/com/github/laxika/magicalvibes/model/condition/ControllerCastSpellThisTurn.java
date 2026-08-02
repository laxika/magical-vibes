package com.github.laxika.magicalvibes.model.condition;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * The controller has cast a spell matching the predicate this turn, including the source card
 * itself if it was the matching spell. Contrast {@link ControllerCastAnotherSpellThisTurn},
 * which excludes the source. Used by Mogg Conscripts ("unless you've cast a creature spell
 * this turn" — the creature's own casting counts).
 */
public record ControllerCastSpellThisTurn(CardPredicate filter) implements Condition {

    @Override
    public String conditionName() {
        return "you cast a matching spell this turn";
    }

    @Override
    public String conditionNotMetReason() {
        return "you haven't cast a matching spell this turn";
    }
}
