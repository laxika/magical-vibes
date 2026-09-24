package com.github.laxika.magicalvibes.model.condition;

/** The source controller is not the player who started the game. */
public record ControllerIsNotStartingPlayer() implements Condition {

    @Override
    public String conditionName() {
        return "controller is not the starting player";
    }

    @Override
    public String conditionNotMetReason() {
        return "controller is the starting player";
    }
}
