package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;

@CardRegistration(set = "ANB", collectorNumber = "52")
@CardRegistration(set = "MB2", collectorNumber = "1")
public class MarduOutrider extends Card {

    public MarduOutrider() {
        // As an additional cost to cast this spell, discard a card.
        addEffect(EffectSlot.SPELL, new DiscardCardTypeCost(null, null));
    }
}
