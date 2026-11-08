package com.github.laxika.magicalvibes.model.condition;

/**
 * True when the player the spell or ability is about (its target, or a triggered ability's non-targeting
 * {@code targetId}: "that player") has lost at least {@code minimumAmount} life this turn, from any source (damage
 * causes loss of life). Lolth, Spider Queen's emblem's "if that player lost less than 8 life this turn" is
 * {@code new NotCondition(new TargetPlayerLostLifeThisTurn(8))}.
 */
public record TargetPlayerLostLifeThisTurn(int minimumAmount) implements Condition {

    public TargetPlayerLostLifeThisTurn {
        if (minimumAmount < 1) {
            throw new IllegalArgumentException("Minimum life lost must be positive");
        }
    }

    /** "That player lost life this turn". */
    public TargetPlayerLostLifeThisTurn() {
        this(1);
    }

    @Override
    public String conditionName() {
        return minimumAmount == 1
                ? "that player lost life this turn"
                : "that player lost " + minimumAmount + " or more life this turn";
    }

    @Override
    public String conditionNotMetReason() {
        return minimumAmount == 1
                ? "that player has not lost life this turn"
                : "that player has not lost " + minimumAmount + " or more life this turn";
    }
}
