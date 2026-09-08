package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;

@CardRegistration(set = "SNC", collectorNumber = "236")
public class ChromeCat extends Card {

    public ChromeCat() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ScryEffect(1));
    }
}
