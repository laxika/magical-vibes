package com.github.laxika.magicalvibes.model.effect;

/** Describes a choice made during the pregame procedure from an opening-hand card. */
public interface PregameChoiceEffect extends CardEffect {

    /** Whether the choice is available only to a player who is not starting the game. */
    default boolean onlyForNonStartingPlayer() {
        return false;
    }
}
