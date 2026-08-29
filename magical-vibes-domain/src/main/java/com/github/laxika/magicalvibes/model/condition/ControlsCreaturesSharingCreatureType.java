package com.github.laxika.magicalvibes.model.condition;

/** The controller controls at least {@code minimum} creatures that share one creature type. */
public record ControlsCreaturesSharingCreatureType(int minimum) implements Condition {

    @Override
    public String conditionName() {
        return "controls " + minimum + " or more creatures sharing a creature type";
    }

    @Override
    public String conditionNotMetReason() {
        return "controller controls fewer than " + minimum + " creatures sharing a creature type";
    }

    @Override
    public boolean isEtbTriggerGate() {
        return true;
    }
}
