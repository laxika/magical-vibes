package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;

/**
 * ETB drawback: sacrifice the source unless the controller discards a card. {@code requiredType}
 * restricts which cards satisfy the discard ({@code null} = any card). When {@code random} is true
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
                                                   CardEffect thenEffect) implements CardEffect {

    public SacrificeUnlessDiscardCardTypeEffect(CardType requiredType) {
        this(requiredType, false, false, null);
    }

    public SacrificeUnlessDiscardCardTypeEffect(CardType requiredType, boolean random) {
        this(requiredType, random, false, null);
    }

    public SacrificeUnlessDiscardCardTypeEffect(CardType requiredType, boolean random,
                                                boolean drawCardIfNotDiscarded) {
        this(requiredType, random, drawCardIfNotDiscarded, null);
    }

    public SacrificeUnlessDiscardCardTypeEffect(CardType requiredType, CardEffect thenEffect) {
        this(requiredType, false, false, thenEffect);
    }
}
