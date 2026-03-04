package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import java.util.Set;

public record SearchLibraryForCardTypesToHandEffect(Set<CardType> cardTypes, int minManaValue, int maxManaValue) implements CardEffect {

    public SearchLibraryForCardTypesToHandEffect(Set<CardType> cardTypes) {
        this(cardTypes, 0, Integer.MAX_VALUE);
    }

    public SearchLibraryForCardTypesToHandEffect(Set<CardType> cardTypes, int maxManaValue) {
        this(cardTypes, 0, maxManaValue);
    }
}
