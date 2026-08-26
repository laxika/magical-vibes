package com.github.laxika.magicalvibes.model.condition;

/**
 * The controller has not cast any spell during this game.
 */
public record ControllerHasNotCastSpellThisGame() implements Condition {

    @Override
    public String conditionName() {
        return "you haven't cast a spell this game";
    }

    @Override
    public String conditionNotMetReason() {
        return "you have already cast a spell this game";
    }
}
