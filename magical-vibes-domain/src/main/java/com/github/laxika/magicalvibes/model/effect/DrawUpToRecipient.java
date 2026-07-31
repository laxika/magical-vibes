package com.github.laxika.magicalvibes.model.effect;

/**
 * Who picks and draws for a {@link DrawUpToNCardsEffect}.
 */
public enum DrawUpToRecipient {

    /** The controller of the resolving stack entry. */
    CONTROLLER,

    /** An opponent of the controller of the resolving stack entry. */
    OPPONENT
}
