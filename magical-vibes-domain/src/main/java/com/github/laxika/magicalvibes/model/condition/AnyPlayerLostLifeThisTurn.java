package com.github.laxika.magicalvibes.model.condition;

/** Matches when any player has lost at least the given amount of life this turn. */
public record AnyPlayerLostLifeThisTurn(int minimumAmount) implements Condition {

    @Override
    public String conditionName() {
        return minimumAmount <= 1
                ? "a player lost life this turn"
                : "a player lost " + minimumAmount + " or more life this turn";
    }

    @Override
    public String conditionNotMetReason() {
        return minimumAmount <= 1
                ? "no player lost life this turn"
                : "no player lost " + minimumAmount + " or more life this turn";
    }

    @Override
    public boolean isEtbTriggerGate() {
        return true;
    }
}
