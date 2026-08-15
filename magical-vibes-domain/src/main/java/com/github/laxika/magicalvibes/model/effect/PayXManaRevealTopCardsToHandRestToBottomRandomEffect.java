package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * On resolution, the controller may pay generic {@code X}; if they do, the top X cards of their
 * library are revealed, matching cards go to their hand, and the rest go to the bottom in a
 * random order. Choosing X=0 declines.
 */
public record PayXManaRevealTopCardsToHandRestToBottomRandomEffect(CardPredicate filter)
        implements CardEffect {
}
