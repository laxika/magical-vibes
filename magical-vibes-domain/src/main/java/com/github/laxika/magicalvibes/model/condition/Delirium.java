package com.github.laxika.magicalvibes.model.condition;

/** Delirium: four or more card types among cards in the controller's graveyard. */
public record Delirium() implements Condition {

    @Override
    public String conditionName() {
        return "delirium";
    }

    @Override
    public String conditionNotMetReason() {
        return "fewer than four card types among cards in graveyard";
    }

    @Override
    public boolean isEtbTriggerGate() {
        return true;
    }
}
