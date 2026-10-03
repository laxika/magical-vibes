package com.github.laxika.magicalvibes.model.condition;

/** Whether one or more poisoned players are being attacked by the attacking player. */
public record AttackedPlayerPoisoned() implements Condition {

    @Override
    public String conditionName() {
        return "an attacked player is poisoned";
    }

    @Override
    public String conditionNotMetReason() {
        return "no attacked player is poisoned";
    }
}
