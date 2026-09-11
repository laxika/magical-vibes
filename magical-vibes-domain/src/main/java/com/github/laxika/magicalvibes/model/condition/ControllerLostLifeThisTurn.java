package com.github.laxika.magicalvibes.model.condition;

/** Matches when the source permanent's controller has lost at least the given amount of life this turn. */
public record ControllerLostLifeThisTurn(int minimumAmount) implements Condition {

    @Override
    public String conditionName() {
        return minimumAmount <= 1
                ? "you lost life this turn"
                : "you lost " + minimumAmount + " or more life this turn";
    }

    @Override
    public String conditionNotMetReason() {
        return minimumAmount <= 1
                ? "you didn't lose life this turn"
                : "you didn't lose " + minimumAmount + " or more life this turn";
    }
}
