package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DoubleCountersOnControlledPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.MultiplyTokenCreationEffect;

@CardRegistration(set = "FDN", collectorNumber = "216")
@CardRegistration(set = "RAV", collectorNumber = "158")
public class DoublingSeason extends Card {

    public DoublingSeason() {
        addEffect(EffectSlot.STATIC, new MultiplyTokenCreationEffect(2));
        addEffect(EffectSlot.STATIC, new DoubleCountersOnControlledPermanentsEffect());
    }
}
