package com.github.laxika.magicalvibes.model.condition;

/** Every player's life total is at or below the threshold (Cryptolith Fragment). */
public record EachPlayerLifeAtMost(int threshold) implements Condition {

    @Override
    public String conditionName() {
        return "each player at or below " + threshold + " life";
    }

    @Override
    public String conditionNotMetReason() {
        return "a player's life total is greater than " + threshold;
    }
}
