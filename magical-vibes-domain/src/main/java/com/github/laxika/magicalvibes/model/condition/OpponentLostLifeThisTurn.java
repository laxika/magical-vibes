package com.github.laxika.magicalvibes.model.condition;

/**
 * An opponent of the controller has lost at least {@code minimumAmount} life this turn (from any
 * source — damage causes loss of life). Reads {@code GameData.lifeLostThisTurn}. Use {@code 3} for
 * Sygg, River Cutthroat ("if an opponent lost 3 or more life this turn").
 */
public record OpponentLostLifeThisTurn(int minimumAmount) implements Condition {

    @Override
    public String conditionName() {
        return minimumAmount <= 1
                ? "an opponent lost life this turn"
                : "an opponent lost " + minimumAmount + " or more life this turn";
    }

    @Override
    public String conditionNotMetReason() {
        return minimumAmount <= 1
                ? "no opponent lost life this turn"
                : "no opponent lost " + minimumAmount + " or more life this turn";
    }

    @Override
    public boolean isEtbTriggerGate() {
        return true;
    }
}
