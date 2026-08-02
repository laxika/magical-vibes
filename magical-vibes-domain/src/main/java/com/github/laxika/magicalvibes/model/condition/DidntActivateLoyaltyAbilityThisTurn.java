package com.github.laxika.magicalvibes.model.condition;

/** The controller hasn't activated a loyalty ability of a planeswalker this turn (The Chain Veil). */
public record DidntActivateLoyaltyAbilityThisTurn() implements Condition {

    @Override
    public String conditionName() {
        return "no loyalty ability activated this turn";
    }

    @Override
    public String conditionNotMetReason() {
        return "a loyalty ability was activated this turn";
    }
}
