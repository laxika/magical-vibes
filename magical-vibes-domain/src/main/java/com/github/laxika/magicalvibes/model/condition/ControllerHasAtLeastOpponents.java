package com.github.laxika.magicalvibes.model.condition;

/** True when the controller has at least {@code minimum} opponents in the game. */
public record ControllerHasAtLeastOpponents(int minimum) implements Condition {

    @Override
    public String conditionName() {
        return "you have at least " + minimum + " opponents";
    }

    @Override
    public String conditionNotMetReason() {
        return "you have fewer than " + minimum + " opponents";
    }
}
