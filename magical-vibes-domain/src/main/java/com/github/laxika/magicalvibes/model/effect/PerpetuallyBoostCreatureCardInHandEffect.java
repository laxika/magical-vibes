package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Keyword;

import java.util.Set;

/** Lets the controller choose a creature card in hand and perpetually modifies that card. */
public record PerpetuallyBoostCreatureCardInHandEffect(int powerBoost, Set<Keyword> keywords)
        implements CardEffect {

    public PerpetuallyBoostCreatureCardInHandEffect {
        keywords = Set.copyOf(keywords);
    }
}
