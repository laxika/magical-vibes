package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** Randomly chooses one matching card from the controller's library and puts it into their hand. */
public record SeekLibraryToHandEffect(CardPredicate filter) implements CardEffect {
}
