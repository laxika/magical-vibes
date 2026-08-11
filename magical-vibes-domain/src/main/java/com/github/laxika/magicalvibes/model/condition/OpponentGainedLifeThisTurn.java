package com.github.laxika.magicalvibes.model.condition;

/**
 * An opponent of the controller has gained at least {@code minimumAmount} life this turn.
 */
public record OpponentGainedLifeThisTurn(int minimumAmount) implements Condition {

    public OpponentGainedLifeThisTurn() {
        this(1);
    }

    @Override
    public String conditionName() {
        return minimumAmount <= 1
                ? "an opponent gained life this turn"
                : "an opponent gained " + minimumAmount + " or more life this turn";
    }

    @Override
    public String conditionNotMetReason() {
        return minimumAmount <= 1
                ? "no opponent gained life this turn"
                : "no opponent gained " + minimumAmount + " or more life this turn";
    }
}
