package com.github.laxika.magicalvibes.model.condition;

/** Raid: the controller attacked with a creature this turn. */
public record Raid() implements Condition {

    @Override
    public String conditionName() {
        return "raid";
    }

    @Override
    public String conditionNotMetReason() {
        return "you didn't attack this turn";
    }

    @Override
    public boolean isEtbTriggerGate() {
        return true;
    }
}
