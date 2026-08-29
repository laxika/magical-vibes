package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** Searches the controller's outside-the-game pool or face-up exile for a card to put into hand. */
public record SearchOutsideGameOrExileForCardToHandEffect(CardPredicate filter) implements CardEffect {
}
