package com.github.laxika.magicalvibes.model.condition;

/**
 * The previous turn's active player cast two or more spells during that turn, the day/night test
 * of CR 731.2b used by nightbound faces. Older werewolf back faces say "a player" and use
 * {@link TwoOrMoreSpellsCastLastTurn} instead.
 */
public record PreviousActivePlayerCastTwoOrMoreSpellsLastTurn() implements Condition {

    @Override
    public String conditionName() {
        return "the previous turn's active player cast two or more spells last turn";
    }

    @Override
    public String conditionNotMetReason() {
        return "the previous turn's active player did not cast two or more spells last turn";
    }
}
