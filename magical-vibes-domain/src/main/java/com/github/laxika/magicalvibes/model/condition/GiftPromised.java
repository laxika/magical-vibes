package com.github.laxika.magicalvibes.model.condition;

/** The spell's Gift was promised while it was being cast. */
public record GiftPromised() implements Condition {

    @Override
    public String conditionName() {
        return "gift promised";
    }

    @Override
    public String conditionNotMetReason() {
        return "the gift was not promised";
    }
}
