package com.github.laxika.magicalvibes.model.condition;

/** The source permanent's controller is the game's starting player. */
public record ControllerIsStartingPlayer() implements Condition {

    @Override
    public String conditionName() {
        return "the starting player";
    }

    @Override
    public String conditionNotMetReason() {
        return "controller is not the starting player";
    }
}
