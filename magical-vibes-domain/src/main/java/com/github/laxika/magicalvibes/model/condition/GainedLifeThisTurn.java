package com.github.laxika.magicalvibes.model.condition;

/**
 * Infusion condition: the effect's controller gained at least {@code minimumAmount} life this turn.
 *
 * @param minimumAmount the amount of life that must have been gained; the no-arg constructor uses 1
 *                      ("if you gained life this turn"), while e.g. Angelic Accord uses 4
 *                      ("if you gained 4 or more life this turn")
 */
public record GainedLifeThisTurn(int minimumAmount) implements Condition {

    public GainedLifeThisTurn() {
        this(1);
    }

    @Override
    public String conditionName() {
        return "infusion";
    }

    @Override
    public String conditionNotMetReason() {
        return minimumAmount > 1
                ? "you didn't gain " + minimumAmount + " or more life this turn"
                : "you didn't gain life this turn";
    }
}
