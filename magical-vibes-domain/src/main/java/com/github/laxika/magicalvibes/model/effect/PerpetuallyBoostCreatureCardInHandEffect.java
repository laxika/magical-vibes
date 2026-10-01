package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.Set;

/** Lets the controller choose a creature card in hand and perpetually modifies that card. */
public record PerpetuallyBoostCreatureCardInHandEffect(int powerBoost, Set<Keyword> keywords,
                                                       CardPredicate cardFilter)
        implements CardEffect {

    public PerpetuallyBoostCreatureCardInHandEffect(int powerBoost, Set<Keyword> keywords) {
        this(powerBoost, keywords, new CardTypePredicate(CardType.CREATURE));
    }

    public PerpetuallyBoostCreatureCardInHandEffect {
        keywords = Set.copyOf(keywords);
        if (cardFilter == null) {
            throw new IllegalArgumentException("A card filter is required");
        }
    }
}
