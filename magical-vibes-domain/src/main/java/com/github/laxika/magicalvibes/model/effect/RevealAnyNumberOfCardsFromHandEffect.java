package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * The controller chooses any number of cards matching {@code filter} from their hand to reveal.
 * The number of cards actually revealed is stored on the stack entry for a following effect.
 * When {@code countedFilter} differs from {@code filter}, only revealed cards matching
 * {@code countedFilter} are counted for that following effect, while any card matching
 * {@code filter} may still be revealed (Xyru Specter counts only black cards among the revealed cards).
 */
public record RevealAnyNumberOfCardsFromHandEffect(CardPredicate filter, CardPredicate countedFilter)
        implements CardEffect {

    public RevealAnyNumberOfCardsFromHandEffect(CardPredicate filter) {
        this(filter, filter);
    }
}
