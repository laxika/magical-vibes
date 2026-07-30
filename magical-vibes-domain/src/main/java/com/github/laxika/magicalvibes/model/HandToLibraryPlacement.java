package com.github.laxika.magicalvibes.model;

/**
 * Where cards chosen from a hand are put back on that player's library.
 */
public enum HandToLibraryPlacement {

    /** Always on top, in the order they were chosen (Brainstorm, Stunted Growth). */
    TOP,

    /** Always on the bottom (Amass the Components). */
    BOTTOM,

    /** The player picks a single top-or-bottom destination for every chosen card (Dream Cache). */
    PLAYER_CHOICE
}
