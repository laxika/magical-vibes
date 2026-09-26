package com.github.laxika.magicalvibes.model.condition;

/** At least one opponent has cast two or more spells this turn. */
public record OpponentCastTwoOrMoreSpellsThisTurn() implements Condition {

    @Override
    public String conditionName() {
        return "an opponent cast two or more spells this turn";
    }

    @Override
    public String conditionNotMetReason() {
        return "no opponent cast two or more spells this turn";
    }
}
