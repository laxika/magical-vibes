package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Exile exactly {@code count} matching cards from the caster's graveyard as a casting cost.
 *
 * @param predicate optional filter the exiled cards must match (null = any card)
 * @param label     human-readable quality for prompts/errors
 * @param count     number of cards to exile
 */
public record ExileNCardsFromGraveyardCastingCost(CardPredicate predicate, String label, int count)
        implements CastingCost {

    public ExileNCardsFromGraveyardCastingCost {
        if (count < 1) {
            throw new IllegalArgumentException("count must be positive");
        }
    }
}
