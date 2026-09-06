package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

@CardRegistration(set = "SNC", collectorNumber = "18")
public class InspiringOverseer extends Card {

    public InspiringOverseer() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new GainLifeEffect(1));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DrawCardEffect(1));
    }
}
