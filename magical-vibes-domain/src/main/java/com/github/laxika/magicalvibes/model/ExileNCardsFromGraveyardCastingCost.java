package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Exile matching cards from the caster's graveyard as a casting cost: exactly {@code count} cards,
 * or, when {@code anyNumber} is set, any number of at least {@code count} cards whose combined card
 * types number at least {@code minCardTypes} ("Exile any number of other cards from your graveyard
 * with four or more card types among them").
 *
 * @param predicate    optional filter the exiled cards must match (null = any card)
 * @param label        human-readable quality for prompts/errors
 * @param count        number of cards to exile (the minimum when {@code anyNumber} is set)
 * @param anyNumber    whether the caster may exile more than {@code count} cards
 * @param minCardTypes distinct card types that must be present among the exiled cards (0 = no requirement)
 */
public record ExileNCardsFromGraveyardCastingCost(CardPredicate predicate, String label, int count,
                                                  boolean anyNumber, int minCardTypes)
        implements CastingCost {

    public ExileNCardsFromGraveyardCastingCost {
        if (count < 1) {
            throw new IllegalArgumentException("count must be positive");
        }
    }

    public ExileNCardsFromGraveyardCastingCost(CardPredicate predicate, String label, int count) {
        this(predicate, label, count, false, 0);
    }

    /** Any number of other graveyard cards with at least {@code minCardTypes} card types among them. */
    public static ExileNCardsFromGraveyardCastingCost anyNumberWithCardTypes(int minCardTypes) {
        return new ExileNCardsFromGraveyardCastingCost(null, "other cards", 1, true, minCardTypes);
    }
}
