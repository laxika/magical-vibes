package com.github.laxika.magicalvibes.model.effect;

/**
 * What happens after looking at the top cards of target player's library — the action axis of
 * {@link LookAtTopCardsOfTargetLibraryEffect}.
 */
public enum TargetLibraryAction {
    /** Pure informational look; the cards stay on top in their original order (Dewdrop Spy, Orcish Spy). */
    LOOK_ONLY,
    /** The controller may exile one of the looked-at cards; the rest go back on top (Psychic Surgery, Puresight Merrow). */
    MAY_EXILE_ONE,
    /** The controller may have the target player shuffle their library (Visions). */
    MAY_SHUFFLE,
    /** The controller puts one of the looked-at cards into that player's graveyard; the rest go back on top (Cruel Fate, Wu Spy). */
    PUT_ONE_INTO_GRAVEYARD,
    /** The controller exiles one of the looked-at cards (mandatory); the rest go back on top in any order (Sealed Fate). */
    EXILE_ONE,
    /**
     * The controller may put the single looked-at top card on the bottom of that player's library;
     * declining leaves it on top (Coral Fighters). Only meaningful with {@code count == 1}.
     */
    MAY_PUT_TOP_ON_BOTTOM
}
