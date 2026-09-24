package com.github.laxika.magicalvibes.model.condition;

/** Met while the game is in the declare attackers step. */
public record DuringDeclareAttackers() implements Condition {

    @Override
    public String conditionName() {
        return "during the declare attackers step";
    }

    @Override
    public String conditionNotMetReason() {
        return "not during the declare attackers step";
    }

    @Override
    public boolean isEtbTriggerGate() {
        return true;
    }
}
