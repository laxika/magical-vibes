package com.github.laxika.magicalvibes.model.condition;

/** The controller's party contains a Cleric, Rogue, Warrior, and Wizard. */
public record FullParty() implements Condition {

    @Override
    public String conditionName() {
        return "full party";
    }

    @Override
    public String conditionNotMetReason() {
        return "fewer than four creatures filling distinct party roles";
    }

    @Override
    public boolean isEtbTriggerGate() {
        return true;
    }
}
