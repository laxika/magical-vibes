package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;

import java.util.Set;

public record EnterPermanentsOfTypesTappedEffect(Set<CardType> cardTypes, boolean opponentsOnly) implements CardEffect {

    public EnterPermanentsOfTypesTappedEffect(Set<CardType> cardTypes) {
        this(cardTypes, false);
    }
}
