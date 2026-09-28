package com.github.laxika.magicalvibes.model.condition;

/** Whether the attacking creature attacked the player who is currently the monarch. */
public record AttackedTargetIsMonarch() implements Condition {

    @Override
    public String conditionName() {
        return "attacked target is the monarch";
    }

    @Override
    public String conditionNotMetReason() {
        return "attacked target is not the monarch";
    }
}
