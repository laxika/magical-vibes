package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Randomly chooses one matching card from the controller's library and puts it onto the
 * battlefield. Unlike a search, this does not reveal cards, create a player interaction, or mark
 * the library as searched.
 */
public record SeekLibraryEffect(CardPredicate filter, int maxManaValue, boolean entersTapped)
        implements CardEffect {

    public SeekLibraryEffect(CardPredicate filter, int maxManaValue) {
        this(filter, maxManaValue, false);
    }
}
