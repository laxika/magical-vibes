package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "AKH", collectorNumber = "151")
public class TormentingVoice extends Card {

    public TormentingVoice() {
        // As an additional cost to cast this spell, discard a card.
        addEffect(EffectSlot.SPELL, new DiscardCardTypeCost(null, null));
        // Draw two cards.
        addEffect(EffectSlot.SPELL, new DrawCardEffect(2));
    }
}
