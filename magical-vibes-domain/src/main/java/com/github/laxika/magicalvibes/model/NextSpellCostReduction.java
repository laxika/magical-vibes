package com.github.laxika.magicalvibes.model;

import java.util.Set;

/** An unconsumed generic-mana reduction for the next matching spell this turn. */
public record NextSpellCostReduction(Set<CardType> cardTypes, int amount) {

    public NextSpellCostReduction {
        cardTypes = Set.copyOf(cardTypes);
        if (amount < 0) {
            throw new IllegalArgumentException("A spell cost reduction cannot be negative");
        }
    }
}
