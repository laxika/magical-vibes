package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Offers the controller one matching card from each graveyard and exiles the chosen cards
 * tracked with the resolving ability's source permanent.
 *
 * @param filter which graveyard cards may be exiled
 * @param requireOne when true ("Exile an instant or sorcery card from each graveyard") one card must be
 *                   chosen from every graveyard holding a match; when false ("up to one") any may be skipped
 */
public record ExileUpToOneMatchingCardFromEachGraveyardWithSourceEffect(CardPredicate filter, boolean requireOne)
        implements CardEffect {

    public ExileUpToOneMatchingCardFromEachGraveyardWithSourceEffect(CardPredicate filter) {
        this(filter, false);
    }
}
