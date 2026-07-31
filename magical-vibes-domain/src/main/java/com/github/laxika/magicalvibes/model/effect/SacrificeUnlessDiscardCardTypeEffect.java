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
 */
public record SacrificeUnlessDiscardCardTypeEffect(CardType requiredType, boolean random,
                                                   boolean drawCardIfNotDiscarded) implements CardEffect {

    public SacrificeUnlessDiscardCardTypeEffect(CardType requiredType) {
        this(requiredType, false, false);
    }

    public SacrificeUnlessDiscardCardTypeEffect(CardType requiredType, boolean random) {
        this(requiredType, random, false);
    }
}
