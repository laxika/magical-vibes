package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Exile exactly X matching cards from the caster's graveyard as a casting cost.
 *
 * @param predicate optional filter the exiled cards must match (null = any card)
 * @param label     human-readable quality for prompts/errors (e.g. "blue")
 */
public record ExileXCardsFromGraveyardCastingCost(CardPredicate predicate, String label)
        implements CastingCost {
}
