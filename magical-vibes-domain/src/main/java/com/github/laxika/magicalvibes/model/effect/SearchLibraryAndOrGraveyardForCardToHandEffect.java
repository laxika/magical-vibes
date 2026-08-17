package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** Searches the controller's library and/or graveyard for one matching card to put into hand. */
public record SearchLibraryAndOrGraveyardForCardToHandEffect(CardPredicate filter) implements CardEffect {
}
