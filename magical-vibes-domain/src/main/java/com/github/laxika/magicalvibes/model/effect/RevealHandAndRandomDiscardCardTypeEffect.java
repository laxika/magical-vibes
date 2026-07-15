package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;

/**
 * "Target player reveals their hand and discards a card of {@code cardType} at random." The whole
 * hand is revealed to the controller; then a single card matching {@code cardType} is chosen
 * uniformly at random from the revealed hand and discarded. If the hand holds no matching card,
 * nothing is discarded. Used by Rag Man (creature card).
 *
 * @param cardType the card type the random discard is restricted to
 */
public record RevealHandAndRandomDiscardCardTypeEffect(CardType cardType) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetCategory.PLAYER);
    }
}
