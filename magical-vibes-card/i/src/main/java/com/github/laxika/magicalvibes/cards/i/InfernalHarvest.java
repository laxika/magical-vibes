package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDividedDamageEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnAnyNumberOfPermanentsToHandCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "VIS", collectorNumber = "62")
public class InfernalHarvest extends Card {

    public InfernalHarvest() {
        // As an additional cost to cast this spell, return X Swamps you control to their owner's hand.
        addEffect(EffectSlot.SPELL, new ReturnAnyNumberOfPermanentsToHandCost(
                new PermanentHasSubtypePredicate(CardSubtype.SWAMP)));
        // Infernal Harvest deals X damage divided as you choose among any number of target creatures.
        addEffect(EffectSlot.SPELL, DealDividedDamageEffect.xAmongTargetCreatures());
    }
}
