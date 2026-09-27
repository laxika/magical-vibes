package com.github.laxika.magicalvibes.model.condition;

/** True when the targeted player is the controller of the resolving ability. */
public record TargetPlayerIsController() implements Condition {

    @Override
    public String conditionName() {
        return "that player is you";
    }

    @Override
    public String conditionNotMetReason() {
        return "that player is not you";
    }
}
