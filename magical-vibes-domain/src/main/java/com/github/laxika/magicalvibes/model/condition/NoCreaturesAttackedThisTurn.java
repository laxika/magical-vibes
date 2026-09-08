package com.github.laxika.magicalvibes.model.condition;

/** No creature has been declared as an attacker this turn. */
public record NoCreaturesAttackedThisTurn() implements Condition {

    @Override
    public String conditionName() {
        return "no creatures attacked this turn";
    }

    @Override
    public String conditionNotMetReason() {
        return "a creature attacked this turn";
    }
}
