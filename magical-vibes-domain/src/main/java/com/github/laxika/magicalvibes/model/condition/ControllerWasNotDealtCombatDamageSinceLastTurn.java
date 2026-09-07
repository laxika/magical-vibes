package com.github.laxika.magicalvibes.model.condition;

/** Matches when the source controller has not been dealt combat damage since their last turn. */
public record ControllerWasNotDealtCombatDamageSinceLastTurn() implements Condition {

    @Override
    public String conditionName() {
        return "you haven't been dealt combat damage since your last turn";
    }

    @Override
    public String conditionNotMetReason() {
        return "you were dealt combat damage since your last turn";
    }
}
