package com.github.laxika.magicalvibes.model.condition;

/** The source permanent did not attack this turn. */
public record DidntAttack() implements Condition {

    @Override
    public String conditionName() {
        return "didn't attack";
    }

    @Override
    public String conditionNotMetReason() {
        return "this creature attacked this turn";
    }
}
