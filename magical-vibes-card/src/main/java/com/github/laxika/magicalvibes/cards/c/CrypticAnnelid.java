package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;

@CardRegistration(set = "FUT", collectorNumber = "34")
public class CrypticAnnelid extends Card {

    public CrypticAnnelid() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ScryEffect(1));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ScryEffect(2));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ScryEffect(3));
    }
}
