package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;

import java.util.List;

/**
 * "As this permanent enters, choose a card name."
 *
 * @param excludedTypes card types whose names may not be chosen (e.g. {@code LAND} for Phyrexian Revoker)
 * @param handAccess whether opponents' hands are seen before the choice, and whether the choice is
 *                   restricted to what they reveal
 */
public record ChooseCardNameOnEnterEffect(List<CardType> excludedTypes, HandAccess handAccess) implements ChooseCardNameEffect {

    /** How the choosing player interacts with opponents' hands before naming a card. */
    public enum HandAccess {
        /** No hand is seen; any card name in the game may be chosen (Pithing Needle, Nevermore). */
        NONE,
        /** The controller looks at an opponent's hand, but may still name any card (Sorcerous Spyglass). */
        LOOK_AT_OPPONENT_HAND,
        /**
         * Each opponent reveals their hand and the name must be one of the cards revealed this way
         * (Alhammarret, High Arbiter).
         */
        REVEAL_OPPONENT_HAND
    }

    public ChooseCardNameOnEnterEffect() {
        this(List.of(), HandAccess.NONE);
    }

    public ChooseCardNameOnEnterEffect(List<CardType> excludedTypes) {
        this(excludedTypes, HandAccess.NONE);
    }
}
