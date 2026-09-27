package com.github.laxika.magicalvibes.model.condition;

/** The source controller was not designated as the game's starting player. */
public record ControllerWasNotStartingPlayer() implements Condition {

    @Override
    public String conditionName() {
        return "controller was not the starting player";
    }

    @Override
    public String conditionNotMetReason() {
        return "controller was the starting player";
    }
}
