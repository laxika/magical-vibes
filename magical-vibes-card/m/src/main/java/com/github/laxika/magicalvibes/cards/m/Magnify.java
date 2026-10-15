package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;

@CardRegistration(set = "UDS", collectorNumber = "111")
public class Magnify extends Card {

    public Magnify() {
        addEffect(EffectSlot.SPELL, new BoostAllCreaturesEffect(1, 1));
    }
}
