package com.github.laxika.magicalvibes.model.condition;

/**
 * True when the total number of permanents on the battlefield — every permanent, regardless of
 * controller — is even (Chaos Lord's "if the number of permanents is even"). Zero counts as even.
 */
public record TotalPermanentCountEven() implements Condition {

    @Override
    public String conditionName() {
        return "even permanent count";
    }

    @Override
    public String conditionNotMetReason() {
        return "the number of permanents is odd";
    }
}
