package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.List;

/**
 * "As this permanent enters, discard any number of [matching] cards. It enters with a keyword counter
 * of each listed type if a card discarded this way has the matching keyword."
 *
 * <p>A replacement effect handled while the permanent enters, before its enter-the-battlefield
 * abilities are collected; the permanent is not on the battlefield while the choices are made.
 *
 * @param cardFilter   the cards that may be discarded
 * @param counterTypes keyword counter types and the keywords they grant (one counter per type at most)
 */
public record DiscardCreatureCardsAsEntersForKeywordCountersEffect(CardPredicate cardFilter,
                                                                    List<CounterType> counterTypes)
        implements ReplacementEffect {

    public DiscardCreatureCardsAsEntersForKeywordCountersEffect {
        counterTypes = List.copyOf(counterTypes);
    }
}
