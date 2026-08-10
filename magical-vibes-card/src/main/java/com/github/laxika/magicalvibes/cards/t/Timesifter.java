package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.TimesifterEffect;

@CardRegistration(set = "MRD", collectorNumber = "262")
public class Timesifter extends Card {

    public Timesifter() {
        addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED, new TimesifterEffect());
    }
}
