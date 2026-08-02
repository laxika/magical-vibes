package com.github.laxika.magicalvibes.model.condition;

/** Matches when the controller of the source permanent lost life during the immediately preceding turn. */
public record ControllerLostLifeLastTurn() implements Condition {

    @Override
    public String conditionName() {
        return "you lost life last turn";
    }

    @Override
    public String conditionNotMetReason() {
        return "you didn't lose life last turn";
    }
}
