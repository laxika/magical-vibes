package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;

import java.util.List;

/**
 * As-enters choice of a card type, optionally after looking at an opponent's hand.
 *
 * @param excludedTypes card types that may not be chosen
 * @param lookAtOpponentHand whether the controller looks at an opponent's hand first
 */
public record ChooseCardTypeOnEnterEffect(List<CardType> excludedTypes,
                                          boolean lookAtOpponentHand) implements CardEffect {

    public ChooseCardTypeOnEnterEffect(List<CardType> excludedTypes) {
        this(excludedTypes, false);
    }

    public ChooseCardTypeOnEnterEffect() {
        this(List.of(), false);
    }
}
