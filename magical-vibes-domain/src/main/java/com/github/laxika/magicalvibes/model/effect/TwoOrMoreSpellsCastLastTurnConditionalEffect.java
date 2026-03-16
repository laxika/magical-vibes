package com.github.laxika.magicalvibes.model.effect;

/**
 * Conditional wrapper that only applies when at least one player cast two or more
 * spells during the previous turn. Used by Innistrad werewolf back faces:
 * "At the beginning of each upkeep, if a player cast two or more spells last turn, transform ~."
 */
public record TwoOrMoreSpellsCastLastTurnConditionalEffect(CardEffect wrapped) implements ConditionalEffect {

    @Override
    public String conditionName() {
        return "two or more spells cast last turn";
    }

    @Override
    public String conditionNotMetReason() {
        return "no player cast two or more spells last turn";
    }
}
