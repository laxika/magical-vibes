package com.github.laxika.magicalvibes.model.condition;

/** The spell was cast with its buyback cost paid (MTG Rule 702.27). */
public record BuybackPaid() implements Condition {

    @Override
    public String conditionName() {
        return "buyback";
    }

    @Override
    public String conditionNotMetReason() {
        return "its buyback cost was not paid";
    }
}
