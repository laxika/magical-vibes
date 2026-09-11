package com.github.laxika.magicalvibes.model.filter;

/** Matches permanents whose current controller has at least the requested number of cards in their graveyard. */
public record PermanentControllerGraveyardCountAtLeastPredicate(int minimumGraveyardCards)
        implements PermanentPredicate {
}
