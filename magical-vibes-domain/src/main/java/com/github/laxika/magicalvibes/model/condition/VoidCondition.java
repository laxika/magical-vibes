package com.github.laxika.magicalvibes.model.condition;

/** True when a nonland permanent left the battlefield or a spell was cast for its Warp cost this turn. */
public record VoidCondition() implements Condition {

    @Override
    public String conditionName() {
        return "a nonland permanent left the battlefield or a spell was warped this turn";
    }

    @Override
    public String conditionNotMetReason() {
        return "no nonland permanent left the battlefield and no spell was warped this turn";
    }

    @Override
    public boolean isEtbTriggerGate() {
        return true;
    }
}
