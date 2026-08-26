package com.github.laxika.magicalvibes.model.effect;

/** Describes a pregame choice to begin with the source card on the battlefield. */
public interface PregameBattlefieldChoiceEffect extends CardEffect {

    /** Whether the choice is available only to a player who is not starting the game. */
    default boolean onlyForNonStartingPlayer() {
        return false;
    }
}
