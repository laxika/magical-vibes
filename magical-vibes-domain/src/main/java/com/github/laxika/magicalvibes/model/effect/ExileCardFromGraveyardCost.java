package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;

/**
 * Cost effect that requires exiling a card of the specified type from the controller's graveyard.
 * If requiredType is null, any card in the graveyard qualifies.
 *
 * @param requiredType              the card type required (null = any)
 * @param payExiledCardManaCost     if true, the exiled card's mana cost must also be paid as part of the ability cost
 * @param imprintOnSource           if true, the exiled card is set as the imprinted card on the source permanent
 * @param trackExiledPower          if true, the exiled card's power is stored as the X value (e.g. Corpse Lunge)
 */
public record ExileCardFromGraveyardCost(CardType requiredType, boolean payExiledCardManaCost,
                                         boolean imprintOnSource, boolean trackExiledPower) implements CostEffect {

    public ExileCardFromGraveyardCost(CardType requiredType) {
        this(requiredType, false, false, false);
    }

    public ExileCardFromGraveyardCost(CardType requiredType, boolean payExiledCardManaCost,
                                      boolean imprintOnSource) {
        this(requiredType, payExiledCardManaCost, imprintOnSource, false);
    }
}
