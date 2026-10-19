package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.List;

/**
 * "Return a [first] card and a [second] card from your graveyard to your hand" without targets: the
 * controller picks, on resolution, one card matching each filter (a lone match is returned
 * automatically; a filter with no match is skipped). Every card moves as one graveyard-leave event, so
 * "whenever one or more cards leave your graveyard" abilities trigger once (Sudden Reclamation).
 *
 * @param filters the ordered filters, each satisfied by a different card
 */
public record ReturnOneOfEachFilterFromControllerGraveyardToHandEffect(List<CardPredicate> filters)
        implements CardEffect {

    public ReturnOneOfEachFilterFromControllerGraveyardToHandEffect {
        filters = List.copyOf(filters);
    }
}
