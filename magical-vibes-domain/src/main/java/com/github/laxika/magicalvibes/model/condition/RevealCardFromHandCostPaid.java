package com.github.laxika.magicalvibes.model.condition;

/** Whether the spell's reveal-a-card-from-hand additional cost was paid. */
public record RevealCardFromHandCostPaid() implements Condition {

    @Override
    public String conditionName() {
        return "reveal card from hand cost paid";
    }

    @Override
    public String conditionNotMetReason() {
        return "the reveal card from hand cost was not paid";
    }
}
