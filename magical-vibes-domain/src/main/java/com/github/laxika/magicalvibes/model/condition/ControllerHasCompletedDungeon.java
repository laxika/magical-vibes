package com.github.laxika.magicalvibes.model.condition;

/** The controller has completed at least one dungeon this game. */
public record ControllerHasCompletedDungeon() implements Condition {

    @Override
    public String conditionName() {
        return "a completed dungeon";
    }

    @Override
    public String conditionNotMetReason() {
        return "the controller has not completed a dungeon";
    }
}
