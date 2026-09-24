package com.github.laxika.magicalvibes.model.condition;

/** Whether the attacking player attacked one of this condition's controller's opponents. */
public record PlayerAttacksOneOfYourOpponents() implements Condition {

    @Override
    public String conditionName() {
        return "a player attacks one of your opponents";
    }

    @Override
    public String conditionNotMetReason() {
        return "no player attacked one of your opponents";
    }
}
