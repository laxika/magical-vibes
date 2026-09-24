package com.github.laxika.magicalvibes.model.condition;

/** True when the source effect's controller is the monarch. */
public record ControllerIsMonarch() implements Condition {

    @Override
    public String conditionName() {
        return "controller is the monarch";
    }

    @Override
    public String conditionNotMetReason() {
        return "controller is not the monarch";
    }
}
