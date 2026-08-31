package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

@CardRegistration(set = "ONS", collectorNumber = "28")
public class ExaltedAngel extends Card {

    public ExaltedAngel() {
        addMorph("{2}{W}{W}");
        addEffect(EffectSlot.ON_SELF_DEALS_DAMAGE, new GainLifeEffect(new EventValue()));
    }
}
