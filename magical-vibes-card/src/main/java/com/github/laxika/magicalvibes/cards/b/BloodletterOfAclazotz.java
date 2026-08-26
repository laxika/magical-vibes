package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DoubleOpponentLifeLossEffect;

@CardRegistration(set = "LCI", collectorNumber = "92")
@CardRegistration(set = "LCI", collectorNumber = "336")
public class BloodletterOfAclazotz extends Card {

    public BloodletterOfAclazotz() {
        addEffect(EffectSlot.STATIC, new DoubleOpponentLifeLossEffect());
    }
}
