package com.github.laxika.magicalvibes.model.condition;

/** Whether a player directly attacked the source controller during that player's last turn. */
public record ControllerWasAttackedByPlayerLastTurn() implements Condition {

    @Override
    public String conditionName() {
        return "a player attacked you during their last turn";
    }

    @Override
    public String conditionNotMetReason() {
        return "no player attacked you during their last turn";
    }
}
