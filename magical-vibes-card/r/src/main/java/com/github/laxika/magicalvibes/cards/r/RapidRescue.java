package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MillControllerAndMayReturnMilledPermanentToHandEffect;

@CardRegistration(set = "MSH", collectorNumber = "181")
public class RapidRescue extends Card {

    public RapidRescue() {
        addEffect(EffectSlot.SPELL, new MillControllerAndMayReturnMilledPermanentToHandEffect(2));
        addEffect(EffectSlot.SPELL, new GainLifeEffect(2));
    }
}
