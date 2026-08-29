package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

@CardRegistration(set = "FDN", collectorNumber = "58")
public class BloodthirstyConqueror extends Card {

    public BloodthirstyConqueror() {
        addEffect(EffectSlot.ON_OPPONENT_LOSES_LIFE, new GainLifeEffect(new EventValue()));
    }
}
