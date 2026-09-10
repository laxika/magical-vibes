package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;

import java.util.List;

/**
 * "As this permanent enters, choose a card name."
 *
 * @param excludedTypes card types whose names may not be chosen (e.g. {@code LAND} for Phyrexian Revoker)
 * @param handAccess whether opponents' hands are seen before the choice, and whether the choice is
 *                   restricted to what they reveal
 * @param nonbasicLandOnly whether the choice is restricted to nonbasic land card names
 * @param requiredType if non-null, only card names with this card type may be chosen
 */
public record ChooseCardNameOnEnterEffect(List<CardType> excludedTypes, HandAccess handAccess,
                                           boolean nonbasicLandOnly, CardType requiredType,
                                           boolean excludeBasicLandNames) implements ChooseCardNameEffect {

    public ChooseCardNameOnEnterEffect(List<CardType> excludedTypes, HandAccess handAccess,
                                      boolean nonbasicLandOnly, CardType requiredType) {
        this(excludedTypes, handAccess, nonbasicLandOnly, requiredType, false);
    }

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
        this(List.of(), HandAccess.NONE, false, null);
    }

    public ChooseCardNameOnEnterEffect(List<CardType> excludedTypes) {
        this(excludedTypes, HandAccess.NONE, false, null);
    }

    public ChooseCardNameOnEnterEffect(List<CardType> excludedTypes, HandAccess handAccess) {
        this(excludedTypes, handAccess, false, null);
    }

    public ChooseCardNameOnEnterEffect(List<CardType> excludedTypes, HandAccess handAccess, boolean nonbasicLandOnly) {
        this(excludedTypes, handAccess, nonbasicLandOnly, null);
    }

    public ChooseCardNameOnEnterEffect(CardType requiredType) {
        this(List.of(), HandAccess.NONE, false, requiredType);
    }
}
