package com.github.laxika.magicalvibes.model.condition;

/** The controller is not the game's starting player. */
public record ControllerIsNotStartingPlayer() implements Condition {

    @Override
    public String conditionName() {
        return "not the starting player";
    }

    @Override
    public String conditionNotMetReason() {
        return "the starting player";
    }
}
