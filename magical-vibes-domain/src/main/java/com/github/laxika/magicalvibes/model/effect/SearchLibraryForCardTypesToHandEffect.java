package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import java.util.Set;

public record SearchLibraryForCardTypesToHandEffect(Set<CardType> cardTypes, int maxManaValue) implements CardEffect {

    public SearchLibraryForCardTypesToHandEffect(Set<CardType> cardTypes) {
        this(cardTypes, Integer.MAX_VALUE);
    }
}
