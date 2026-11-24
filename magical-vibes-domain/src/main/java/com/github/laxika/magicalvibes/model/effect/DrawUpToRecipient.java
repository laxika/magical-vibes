package com.github.laxika.magicalvibes.model.effect;

/**
 * Who picks and draws for a {@link DrawUpToNCardsEffect}.
 */
public enum DrawUpToRecipient {

    /** The controller of the resolving stack entry. */
    CONTROLLER,

    /** "That player" of "an opponent chooses one —": the opponent who chose the spell's mode (CR 700.2e). */
    MODE_CHOOSER,

    /** The player retained in the entry's recipient field. */
    TARGET_PLAYER
}
