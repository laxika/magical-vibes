package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExchangeGraveyardAndLibraryEffect;

@CardRegistration(set = "JUD", collectorNumber = "70")
public class MoralityShift extends Card {

    public MoralityShift() {
        addEffect(EffectSlot.SPELL, new ExchangeGraveyardAndLibraryEffect());
    }
}
