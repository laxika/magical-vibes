package com.github.laxika.magicalvibes.model.condition;

/** The source permanent's controller was the monarch when the current turn began. */
public record ControllerWasMonarchAtTurnStart() implements Condition {

    @Override
    public String conditionName() {
        return "the controller was the monarch as the turn began";
    }

    @Override
    public String conditionNotMetReason() {
        return "the controller was not the monarch as the turn began";
    }
}
