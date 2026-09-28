package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** Seeks one matching card into the graveyard, then conjures two duplicates of it there. */
public record SeekLibraryAndConjureDuplicatesInGraveyardEffect(CardPredicate filter)
        implements CardEffect {
}
