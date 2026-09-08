package com.github.laxika.magicalvibes.model.condition;

/** True if the source permanent attacked during its controller's previous turn. */
public record SourceAttackedDuringControllersLastTurn() implements Condition {

    @Override
    public String conditionName() {
        return "source attacked during its controller's last turn";
    }

    @Override
    public String conditionNotMetReason() {
        return "this permanent did not attack during its controller's last turn";
    }
}
