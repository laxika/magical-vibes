package com.github.laxika.magicalvibes.model.condition;

/** Whether the controller has reached speed 4. */
public record MaxSpeed() implements Condition {

    @Override
    public String conditionName() {
        return "max speed";
    }

    @Override
    public String conditionNotMetReason() {
        return "you don't have max speed";
    }
}
