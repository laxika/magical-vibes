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
    /**
     * The controller may exile any number of the looked-at cards, one pick at a time; declining ends
     * the picks and the rest go back on top in any order (Ancestral Knowledge).
     */
    MAY_EXILE_ANY_NUMBER,
    /** The controller may have the target player shuffle their library (Visions). */
    MAY_SHUFFLE,
    /** The controller puts one of the looked-at cards into that player's graveyard; the rest go back on top (Cruel Fate, Wu Spy). */
    PUT_ONE_INTO_GRAVEYARD,
    /** Reveal the looked-at cards publicly, then put one into that player's graveyard and the rest back on top (Balshan Beguiler). */
    REVEAL_AND_PUT_ONE_INTO_GRAVEYARD,
    /** The controller exiles one of the looked-at cards (mandatory); the rest go back on top in any order (Sealed Fate). */
    EXILE_ONE,
    /** The controller exiles one card face down, grants a cast permission for it, and puts the rest on the bottom randomly (Gonti, Lord of Luxury). */
    EXILE_ONE_FACE_DOWN_REST_TO_BOTTOM_RANDOM,
    /** The controller exiles one card face down, grants a cast permission for it, and puts the rest into the target player's graveyard (Thief of Sanity). */
    EXILE_ONE_FACE_DOWN_REST_TO_GRAVEYARD,
    /**
     * The controller may put the single looked-at top card on the bottom of that player's library;
     * declining leaves it on top (Coral Fighters). Only meaningful with {@code count == 1}.
     */
    MAY_PUT_TOP_ON_BOTTOM,
    /**
     * The <em>target player</em> — not the controller — looks at the cards, puts one of them back
     * on top of their library, and the rest are exiled (Ashnod's Cylix).
     */
    KEEP_ONE_ON_TOP_EXILE_REST,
    /**
     * The controller looks at the cards, puts one of them back on top of the target player's
     * library, and the rest go into that player's graveyard (Dimir Charm). The pick is mandatory.
     */
    KEEP_ONE_ON_TOP_REST_TO_GRAVEYARD
}
