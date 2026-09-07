package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

@CardRegistration(set = "IKO", collectorNumber = "158")
public class HoneyMammoth extends Card {

    public HoneyMammoth() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new GainLifeEffect(4));
    }
}
