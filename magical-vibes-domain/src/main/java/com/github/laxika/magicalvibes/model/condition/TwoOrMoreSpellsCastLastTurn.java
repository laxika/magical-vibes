package com.github.laxika.magicalvibes.model.condition;

/** A player cast two or more spells during the previous turn (werewolf back faces). */
public record TwoOrMoreSpellsCastLastTurn() implements Condition {

    @Override
    public String conditionName() {
        return "two or more spells cast last turn";
    }

    @Override
    public String conditionNotMetReason() {
        return "no player cast two or more spells last turn";
    }
}
