package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;

/**
 * ETB drawback: sacrifice the source unless the controller discards a card. {@code requiredType}
 * restricts which cards satisfy the discard ({@code null} = any card). When {@code random} is true
 * the discarded card is chosen at random (Pillaging Horde) rather than by the player (Hidden Horror);
 * random discard only makes sense with {@code requiredType == null}.
 */
public record SacrificeUnlessDiscardCardTypeEffect(CardType requiredType, boolean random) implements CardEffect {

    public SacrificeUnlessDiscardCardTypeEffect(CardType requiredType) {
        this(requiredType, false);
    }
}
