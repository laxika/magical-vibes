package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;

import java.util.List;

/**
 * As-enters choice of a card type, optionally after looking at an opponent's hand.
 *
 * @param excludedTypes card types that may not be chosen
 * @param lookAtOpponentHand whether the controller looks at an opponent's hand first
 * @param sharedWithCraftMaterials whether the legal choices are restricted to types shared by
 *                                 the two materials used to craft the permanent
 */
public record ChooseCardTypeOnEnterEffect(List<CardType> excludedTypes,
                                          boolean lookAtOpponentHand,
                                          boolean sharedWithCraftMaterials) implements CardEffect {

    public ChooseCardTypeOnEnterEffect(List<CardType> excludedTypes) {
        this(excludedTypes, false, false);
    }

    public ChooseCardTypeOnEnterEffect(List<CardType> excludedTypes, boolean lookAtOpponentHand) {
        this(excludedTypes, lookAtOpponentHand, false);
    }

    public ChooseCardTypeOnEnterEffect() {
        this(List.of(), false, false);
    }

    public static ChooseCardTypeOnEnterEffect forSharedCraftMaterials() {
        return new ChooseCardTypeOnEnterEffect(List.of(), false, true);
    }
}
