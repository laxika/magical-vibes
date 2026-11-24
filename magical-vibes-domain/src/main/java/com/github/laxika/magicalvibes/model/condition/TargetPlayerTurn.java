package com.github.laxika.magicalvibes.model.condition;

/**
 * The turn of the player the condition is judged for is in progress: the targeted player, or the player a static cast
 * lock is judged for ("players can cast spells only during their own turns" restricts while it isn't met).
 */
public record TargetPlayerTurn() implements Condition {

    @Override
    public String conditionName() {
        return "the targeted player's turn";
    }

    @Override
    public String conditionNotMetReason() {
        return "it is not the targeted player's turn";
    }
}
