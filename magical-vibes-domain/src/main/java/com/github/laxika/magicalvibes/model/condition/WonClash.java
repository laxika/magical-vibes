package com.github.laxika.magicalvibes.model.condition;

/**
 * The controller won their most recent clash (MTG Rule 701.29). Written by the clash-source effect
 * during resolution and read by a later effect on the same stack entry — e.g. Whirlpool Whelm's
 * "If you win, you may put that creature on top of its owner's library instead."
 */
public record WonClash() implements Condition {

    @Override
    public String conditionName() {
        return "won the clash";
    }

    @Override
    public String conditionNotMetReason() {
        return "you didn't win the clash";
    }
}
