package com.github.laxika.magicalvibes.model.condition;

/** The controller attacked with a commander during this turn. */
public record AttackedWithCommanderThisTurn() implements Condition {

    @Override
    public String conditionName() {
        return "attacked with a commander this turn";
    }

    @Override
    public String conditionNotMetReason() {
        return "you didn't attack with a commander this turn";
    }
}
