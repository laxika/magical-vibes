package com.github.laxika.magicalvibes.model.condition;

/** The source permanent's controller is the monarch. */
public record ControllerIsMonarch() implements Condition {

    @Override
    public String conditionName() {
        return "the monarch";
    }

    @Override
    public String conditionNotMetReason() {
        return "controller is not the monarch";
    }
}
