package com.github.laxika.magicalvibes.model.condition;

/** Whether the targeted player directly attacked the condition's controller during that player's last turn. */
public record TargetPlayerAttackedControllerLastTurn() implements Condition {

    @Override
    public String conditionName() {
        return "that player attacked you during their last turn";
    }

    @Override
    public String conditionNotMetReason() {
        return "that player did not attack you during their last turn";
    }
}
