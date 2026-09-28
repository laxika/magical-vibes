package com.github.laxika.magicalvibes.model.condition;

/** True when the targeted player attacked the source controller during that player's last turn. */
public record TargetPlayerAttackedControllerDuringLastTurn() implements Condition {

    @Override
    public String conditionName() {
        return "the targeted player attacked you during their last turn";
    }

    @Override
    public String conditionNotMetReason() {
        return "the targeted player did not attack you during their last turn";
    }
}
