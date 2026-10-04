package com.github.laxika.magicalvibes.model.condition;

/** The controller cast a spell from a graveyard or activated an ability of a graveyard card this turn. */
public record ControllerCastSpellFromGraveyardOrActivatedGraveyardAbilityThisTurn() implements Condition {

    @Override
    public String conditionName() {
        return "you cast a spell from a graveyard or activated an ability of a graveyard card this turn";
    }

    @Override
    public String conditionNotMetReason() {
        return "you haven't cast a spell from a graveyard or activated an ability of a graveyard card this turn";
    }
}
