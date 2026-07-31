package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "M14", collectorNumber = "63")
public class MessengerDrake extends Card {

    public MessengerDrake() {
        addEffect(EffectSlot.ON_DEATH, new DrawCardEffect());
    }
}
