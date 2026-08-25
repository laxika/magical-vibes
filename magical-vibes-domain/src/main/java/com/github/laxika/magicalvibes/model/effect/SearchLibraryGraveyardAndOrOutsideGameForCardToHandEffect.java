package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** Searches the controller's library, graveyard, and outside-the-game cards for one matching card. */
public record SearchLibraryGraveyardAndOrOutsideGameForCardToHandEffect(CardPredicate filter)
        implements CardEffect {
}
