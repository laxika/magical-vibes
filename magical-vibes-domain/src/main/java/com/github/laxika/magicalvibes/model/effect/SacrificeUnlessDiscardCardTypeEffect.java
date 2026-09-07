package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

/**
 * ETB drawback: sacrifice the source unless the controller discards the required number of cards.
 * {@code requiredType}
 * restricts which cards satisfy the discard ({@code null} = any card). A predicate supplied through
 * {@link #forPredicate(CardPredicate, String)} supports filters such as noncreature cards. When {@code random} is true
 * the discarded card is chosen at random (Pillaging Horde) rather than by the player (Hidden Horror);
 * random discard only makes sense with {@code requiredType == null}.
 *
 * <p>{@code drawCardIfNotDiscarded} adds the "and draw a card" rider to the penalty branch
 * (Lim-D&ucirc;l's Paladin: "you may discard a card. If you don't, sacrifice this creature and draw a
 * card."). The draw happens whenever the discard doesn't — including when the hand is empty and when
 * the source has already left the battlefield, since each part is done as much as possible.
 *
 * <p>When {@code thenEffect} is present, it is pushed as a reflexive trigger after a successful
 * discard. Target filters on that effect receive the discarded card's mana value as X.
 */
public record SacrificeUnlessDiscardCardTypeEffect(CardType requiredType, boolean random,
                                                   boolean drawCardIfNotDiscarded,
                                                   CardEffect thenEffect, CardPredicate discardPredicate,
                                                   String discardDescription, int discardCount) implements CardEffect {

    public SacrificeUnlessDiscardCardTypeEffect(CardType requiredType) {
        this(requiredType, false, false, null, null, null, 1);
    }

    public SacrificeUnlessDiscardCardTypeEffect(CardType requiredType, boolean random) {
        this(requiredType, random, false, null, null, null, 1);
    }

    public SacrificeUnlessDiscardCardTypeEffect(CardType requiredType, boolean random,
                                                boolean drawCardIfNotDiscarded) {
        this(requiredType, random, drawCardIfNotDiscarded, null, null, null, 1);
    }

    public SacrificeUnlessDiscardCardTypeEffect(CardType requiredType, CardEffect thenEffect) {
        this(requiredType, false, false, thenEffect, null, null, 1);
    }

    public SacrificeUnlessDiscardCardTypeEffect(CardType requiredType, int discardCount) {
        this(requiredType, false, false, null, null, null, discardCount);
    }

    public static SacrificeUnlessDiscardCardTypeEffect forPredicate(CardPredicate predicate,
                                                                      String description) {
        return new SacrificeUnlessDiscardCardTypeEffect(null, false, false, null, predicate, description, 1);
    }

    public static SacrificeUnlessDiscardCardTypeEffect forPredicate(CardPredicate predicate,
                                                                      String description, int discardCount) {
        return new SacrificeUnlessDiscardCardTypeEffect(null, false, false, null, predicate, description, discardCount);
    }

    public CardPredicate discardPredicate() {
        return discardPredicate != null
                ? discardPredicate
                : requiredType == null ? null : new CardTypePredicate(requiredType);
    }

    public String discardDescription() {
        return discardDescription != null
                ? discardDescription
                : requiredType == null ? "card" : requiredType.name().toLowerCase() + " card";
    }
}
