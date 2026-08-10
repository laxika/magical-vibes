package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Searches the controller's library for any number of cards matching {@code filter}, exiles the
 * selected cards, and creates one {@code tokenTemplate} token for each card selected.
 */
public record SearchLibraryForCardsToExileAndCreateTokensEffect(
        CardPredicate filter,
        CreateTokenEffect tokenTemplate
) implements CardEffect {
}
