package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** Searches the controller's library for one matching card to put into their hand. */
public record SearchLibraryForCardToHandOrCreateTokenEffect(
        CardPredicate filter,
        CreateTokenEffect tokenTemplate
) implements CardEffect {
}
