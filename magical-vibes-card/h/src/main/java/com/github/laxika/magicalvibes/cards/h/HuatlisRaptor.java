package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ProliferateEffect;

@CardRegistration(set = "WAR", collectorNumber = "200")
public class HuatlisRaptor extends Card {

    public HuatlisRaptor() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ProliferateEffect());
    }
}
