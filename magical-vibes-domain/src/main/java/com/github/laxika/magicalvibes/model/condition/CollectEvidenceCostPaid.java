package com.github.laxika.magicalvibes.model.condition;

/** The spell's optional collect-evidence additional cost was paid. */
public record CollectEvidenceCostPaid() implements Condition {

    @Override
    public boolean isEtbTriggerGate() {
        return true;
    }

    @Override
    public String conditionName() {
        return "collect-evidence cost paid";
    }

    @Override
    public String conditionNotMetReason() {
        return "the collect-evidence cost was not paid";
    }
}
