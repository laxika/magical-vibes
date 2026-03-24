package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;

import java.util.List;

public record ChooseCardNameOnEnterEffect(List<CardType> excludedTypes, boolean lookAtOpponentHand) implements ChooseCardNameEffect {

    public ChooseCardNameOnEnterEffect() {
        this(List.of(), false);
    }

    public ChooseCardNameOnEnterEffect(List<CardType> excludedTypes) {
        this(excludedTypes, false);
    }
}
