package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;

/**
 * Cost effect that requires exiling a card of the specified type from the controller's graveyard.
 * If requiredType is null, any card in the graveyard qualifies.
 *
 * @param requiredType              the card type required (null = any)
 * @param payExiledCardManaCost     if true, the exiled card's mana cost must also be paid as part of the ability cost
 * @param imprintOnSource           if true, the exiled card is set as the imprinted card on the source permanent
 */
public record ExileCardFromGraveyardCost(CardType requiredType, boolean payExiledCardManaCost,
                                         boolean imprintOnSource) implements CostEffect {

    public ExileCardFromGraveyardCost(CardType requiredType) {
        this(requiredType, false, false);
    }
}
