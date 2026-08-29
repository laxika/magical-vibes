package com.github.laxika.magicalvibes.model.filter;

public record CardMinManaValuePredicate(int minManaValue, boolean includeXValue) implements CardPredicate {

    public CardMinManaValuePredicate(int minManaValue) {
        this(minManaValue, false);
    }
}
