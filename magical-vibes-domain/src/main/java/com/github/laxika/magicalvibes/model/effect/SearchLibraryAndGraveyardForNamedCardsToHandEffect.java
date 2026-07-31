package com.github.laxika.magicalvibes.model.effect;

import java.util.List;

/**
 * Searches the controller's library and graveyard for up to one card of each listed name, reveals
 * those found, puts them into hand, then shuffles. Graveyard matches are taken automatically; names
 * still needed after that are offered as optional library picks (may fail to find any or all). The
 * library is always shuffled once the search finishes (unless library search is prevented). Used by
 * Nissa's Encouragement.
 */
public record SearchLibraryAndGraveyardForNamedCardsToHandEffect(List<String> cardNames)
        implements CardEffect {

    public SearchLibraryAndGraveyardForNamedCardsToHandEffect {
        cardNames = List.copyOf(cardNames);
    }
}
