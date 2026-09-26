package com.github.laxika.magicalvibes.model.condition;

/** The controller has lost a game in the current match. */
public record ControllerLostGameThisMatch() implements Condition {

    @Override
    public String conditionName() {
        return "the controller has lost a game this match";
    }

    @Override
    public String conditionNotMetReason() {
        return "the controller has not lost a game this match";
    }
}
