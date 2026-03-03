package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;

import java.util.List;

public record ChooseCardNameOnEnterEffect(List<CardType> excludedTypes) implements ChooseCardNameEffect {

    public ChooseCardNameOnEnterEffect() {
        this(List.of());
    }
}
