package com.github.laxika.magicalvibes.model.condition;

/** The controller controls strictly more creatures than their opponent. */
public record ControlsMoreCreaturesThanOpponent() implements Condition {

    @Override
    public String conditionName() {
        return "control more creatures than the opponent";
    }

    @Override
    public String conditionNotMetReason() {
        return "do not control more creatures than the opponent";
    }
}
