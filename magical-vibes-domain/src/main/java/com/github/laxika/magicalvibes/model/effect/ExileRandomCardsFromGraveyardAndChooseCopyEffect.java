package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Exiles up to {@code count} cards at random from the controller's graveyard, then offers one
 * matching exiled card to copy and cast for free.
 *
 * @param count  maximum number of cards exiled at random
 * @param filter the cards among the random selections that may be copied
 */
public record ExileRandomCardsFromGraveyardAndChooseCopyEffect(int count, CardPredicate filter)
        implements CardEffect {

    public ExileRandomCardsFromGraveyardAndChooseCopyEffect {
        if (count <= 0) {
            throw new IllegalArgumentException("count must be positive");
        }
    }
}
