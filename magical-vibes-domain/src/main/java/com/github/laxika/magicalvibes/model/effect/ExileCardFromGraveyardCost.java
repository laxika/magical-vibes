package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;

/**
 * Cost effect that requires exiling a card of the specified type from the controller's graveyard.
 * If requiredType is null, any card in the graveyard qualifies.
 */
public record ExileCardFromGraveyardCost(CardType requiredType) implements CostEffect {
}
