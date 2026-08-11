package com.github.laxika.magicalvibes.model.condition;

/** At least one opponent has cast three or more spells this turn. */
public record OpponentCastThreeOrMoreSpellsThisTurn() implements Condition {

    @Override
    public String conditionName() {
        return "an opponent cast three or more spells this turn";
    }

    @Override
    public String conditionNotMetReason() {
        return "no opponent cast three or more spells this turn";
    }
}
