package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CardSubtype;

/**
 * Cost effect that requires exiling a card from the controller's graveyard.
 * If both {@code requiredType} and {@code requiredSubtype} are null, any card in the graveyard qualifies.
 * When set, both filters must be satisfied.
 *
 * @param requiredType              the card type required (null = any)
 * @param payExiledCardManaCost     if true, the exiled card's mana cost must also be paid as part of the ability cost
 * @param imprintOnSource           if true, the exiled card is set as the imprinted card on the source permanent
 * @param trackExiledPower          if true, the exiled card's power is stored as the X value (e.g. Corpse Lunge)
 * @param requiredSubtype           the card subtype required (null = any), e.g. "Exile an Elf card" (Scarred Vinebreeder)
 */
public record ExileCardFromGraveyardCost(CardType requiredType, boolean payExiledCardManaCost,
                                         boolean imprintOnSource, boolean trackExiledPower,
                                         CardSubtype requiredSubtype) implements CostEffect {

    public ExileCardFromGraveyardCost(CardType requiredType) {
        this(requiredType, false, false, false, null);
    }

    public ExileCardFromGraveyardCost(CardSubtype requiredSubtype) {
        this(null, false, false, false, requiredSubtype);
    }

    public ExileCardFromGraveyardCost(CardType requiredType, boolean payExiledCardManaCost,
                                      boolean imprintOnSource) {
        this(requiredType, payExiledCardManaCost, imprintOnSource, false, null);
    }

    public ExileCardFromGraveyardCost(CardType requiredType, boolean payExiledCardManaCost,
                                      boolean imprintOnSource, boolean trackExiledPower) {
        this(requiredType, payExiledCardManaCost, imprintOnSource, trackExiledPower, null);
    }
}
