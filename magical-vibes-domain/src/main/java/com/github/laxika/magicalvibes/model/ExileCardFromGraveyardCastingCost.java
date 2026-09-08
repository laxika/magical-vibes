package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Exile a chosen card from the caster's graveyard as an alternate casting cost.
 *
 * @param predicate optional filter the exiled card must match (null = any card)
 * @param label     human-readable quality for prompts/errors (e.g. "creature")
 */
public record ExileCardFromGraveyardCastingCost(CardPredicate predicate, String label)
        implements CastingCost {
}
