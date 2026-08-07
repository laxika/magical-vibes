package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Exile the top matching card(s) of the caster's graveyard as an alternate casting cost
 * (e.g. Spinning Darkness: "You may exile the top three black cards of your graveyard rather
 * than pay this spell's mana cost").
 *
 * <p>The cards are determined, not chosen — "top" means closest to the top of the graveyard
 * (the most recently put there), skipping cards that do not match {@code predicate}.
 *
 * @param predicate optional filter the exiled cards must match (null = any card)
 * @param label     human-readable quality for prompts/errors (e.g. "black")
 * @param count     number of cards that must be exiled
 */
public record ExileTopCardsFromGraveyardCastingCost(CardPredicate predicate, String label, int count)
        implements CastingCost {

    public ExileTopCardsFromGraveyardCastingCost {
        if (count < 1) {
            throw new IllegalArgumentException("exile count must be >= 1");
        }
    }
}
