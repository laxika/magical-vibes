package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;

/**
 * ETB drawback: exile the source unless its controller discards a card of the required type.
 */
public record ExileUnlessDiscardCardTypeEffect(CardType requiredType) implements CardEffect {
}
