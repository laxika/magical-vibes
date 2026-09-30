package com.github.laxika.magicalvibes.model.condition;

/** The controller currently has at least one player-scoped boon. */
public record ControllerHasBoon() implements Condition {

    @Override
    public String conditionName() {
        return "you have a boon";
    }

    @Override
    public String conditionNotMetReason() {
        return "you do not have a boon";
    }
}
