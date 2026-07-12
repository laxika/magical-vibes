package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEqualToDamageDealtEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "45")
@CardRegistration(set = "9ED", collectorNumber = "47")
@CardRegistration(set = "8ED", collectorNumber = "47")
public class SpiritLink extends Card {

    public SpiritLink() {
        addEffect(EffectSlot.STATIC, new GainLifeEqualToDamageDealtEffect());
    }
}
