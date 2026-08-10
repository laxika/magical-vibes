package com.github.laxika.magicalvibes.model.condition;

/** The controller controls strictly more lands than their opponent. */
public record ControllerControlsMoreLandsThanOpponent() implements Condition {

    @Override
    public String conditionName() {
        return "you control more lands than your opponent";
    }

    @Override
    public String conditionNotMetReason() {
        return "you control no more lands than your opponent";
    }
}
