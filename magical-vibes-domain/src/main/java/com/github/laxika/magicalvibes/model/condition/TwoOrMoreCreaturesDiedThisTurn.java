package com.github.laxika.magicalvibes.model.condition;

/** Two or more creatures died this turn, regardless of who controlled them. */
public record TwoOrMoreCreaturesDiedThisTurn() implements Condition {

    @Override
    public String conditionName() {
        return "two or more creatures died this turn";
    }

    @Override
    public String conditionNotMetReason() {
        return "fewer than two creatures died this turn";
    }
}
