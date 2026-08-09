package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Cost effect requiring the controller to put a card from their hand on top of their library.
 */
public record PutCardFromHandOnTopOfLibraryCost() implements HandCardCost {

    @Override
    public CardPredicate predicate() {
        return null;
    }

    @Override
    public String label() {
        return null;
    }

    @Override
    public int count() {
        return 1;
    }

    @Override
    public boolean putsPaidCardsOnTopOfLibrary() {
        return true;
    }
}
