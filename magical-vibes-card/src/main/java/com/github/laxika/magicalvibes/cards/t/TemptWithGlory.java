package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.TemptingOfferPutCountersEffect;

@CardRegistration(set = "C13", collectorNumber = "24")
public class TemptWithGlory extends Card {

    public TemptWithGlory() {
        addEffect(EffectSlot.SPELL, new TemptingOfferPutCountersEffect());
    }
}
