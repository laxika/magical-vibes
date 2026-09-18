package com.github.laxika.magicalvibes.model.condition;

/** True when the player targeted by the effect has at most the specified life total. */
public record TargetPlayerLifeAtMost(int threshold) implements Condition {

    @Override
    public String conditionName() {
        return "that player has " + threshold + " or less life";
    }

    @Override
    public String conditionNotMetReason() {
        return "that player's life total is above " + threshold;
    }
}
