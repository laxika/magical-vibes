package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

@CardRegistration(set = "CSP", collectorNumber = "132")
public class Tamanoa extends Card {

    public Tamanoa() {
        // Whenever a noncreature source you control deals damage, you gain that much life.
        addEffect(EffectSlot.ON_ANY_SOURCE_DEALS_DAMAGE, new GainLifeEffect(new EventValue()));
    }
}
