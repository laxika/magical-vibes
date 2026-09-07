package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;

@CardRegistration(set = "LCI", collectorNumber = "202")
public class MineshaftSpider extends Card {

    public MineshaftSpider() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new MayEffect(new MillEffect(2, MillRecipient.CONTROLLER), "Mill two cards?"));
    }
}
