package com.github.laxika.magicalvibes.model.effect;

/**
 * Conditional wrapper that only applies when no spells were cast by any player
 * during the previous turn. Used by Innistrad werewolf front faces:
 * "At the beginning of each upkeep, if no spells were cast last turn, transform ~."
 */
public record NoSpellsCastLastTurnConditionalEffect(CardEffect wrapped) implements ConditionalEffect {

    @Override
    public String conditionName() {
        return "no spells cast last turn";
    }

    @Override
    public String conditionNotMetReason() {
        return "a spell was cast last turn";
    }
}
