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
 * @param alternateType             a second acceptable card type (null = none); a card of either type qualifies,
 *                                  e.g. "Exile an instant or sorcery card from your graveyard" (Disciple of the Ring)
 * @param trackExiledManaValue      if true, the exiled card's mana value is stored as the X value
 */
public record ExileCardFromGraveyardCost(CardType requiredType, boolean payExiledCardManaCost,
                                         boolean imprintOnSource, boolean trackExiledPower,
                                         CardSubtype requiredSubtype, CardType alternateType,
                                         boolean trackExiledManaValue) implements CostEffect {

    public ExileCardFromGraveyardCost(CardType requiredType, boolean payExiledCardManaCost,
                                      boolean imprintOnSource, boolean trackExiledPower,
                                      CardSubtype requiredSubtype, CardType alternateType) {
        this(requiredType, payExiledCardManaCost, imprintOnSource, trackExiledPower,
                requiredSubtype, alternateType, false);
    }

    public ExileCardFromGraveyardCost(CardType requiredType) {
        this(requiredType, false, false, false, null, null, false);
    }

    public ExileCardFromGraveyardCost(CardType requiredType, CardType alternateType) {
        this(requiredType, false, false, false, null, alternateType, false);
    }

    public ExileCardFromGraveyardCost(CardSubtype requiredSubtype) {
        this(null, false, false, false, requiredSubtype, null, false);
    }

    public ExileCardFromGraveyardCost(CardType requiredType, boolean payExiledCardManaCost,
                                      boolean imprintOnSource) {
        this(requiredType, payExiledCardManaCost, imprintOnSource, false, null, null, false);
    }

    public ExileCardFromGraveyardCost(CardType requiredType, boolean payExiledCardManaCost,
                                      boolean imprintOnSource, boolean trackExiledPower) {
        this(requiredType, payExiledCardManaCost, imprintOnSource, trackExiledPower, null, null, false);
    }

    public static ExileCardFromGraveyardCost trackingExiledManaValue(CardType requiredType,
                                                                      CardType alternateType) {
        return new ExileCardFromGraveyardCost(requiredType, false, false, false,
                null, alternateType, true);
    }
}
