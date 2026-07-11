package com.github.laxika.magicalvibes.model.condition;

/** No spells were cast by any player during the previous turn (werewolf front faces). */
public record NoSpellsCastLastTurn() implements Condition {

    @Override
    public String conditionName() {
        return "no spells cast last turn";
    }

    @Override
    public String conditionNotMetReason() {
        return "a spell was cast last turn";
    }
}
