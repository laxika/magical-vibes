package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RampageEffect;

@CardRegistration(set = "LEG", collectorNumber = "231")
public class HundingGjornersen extends Card {

    public HundingGjornersen() {
        addEffect(EffectSlot.ON_BECOMES_BLOCKED, new RampageEffect(1));
    }
}
