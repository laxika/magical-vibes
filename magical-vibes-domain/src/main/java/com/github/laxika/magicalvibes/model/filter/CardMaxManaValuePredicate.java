package com.github.laxika.magicalvibes.model.filter;

public record CardMaxManaValuePredicate(int maxManaValue, boolean includeXValue) implements CardPredicate {

    public CardMaxManaValuePredicate(int maxManaValue) {
        this(maxManaValue, false);
    }
}
