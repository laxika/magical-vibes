package com.github.laxika.magicalvibes.model.condition;

/** True when the player targeted by the spell or ability has the specified life total. */
public record TargetPlayerLifeTotalEquals(int lifeTotal) implements Condition {

    @Override
    public String conditionName() {
        return "that player has exactly " + lifeTotal + " life";
    }

    @Override
    public String conditionNotMetReason() {
        return "that player's life total is not exactly " + lifeTotal;
    }
}
