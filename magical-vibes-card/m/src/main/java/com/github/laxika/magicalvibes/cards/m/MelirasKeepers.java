package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantHaveCountersEffect;

@CardRegistration(set = "MBS", collectorNumber = "83")
public class MelirasKeepers extends Card {

    public MelirasKeepers() {
        addEffect(EffectSlot.STATIC, new CantHaveCountersEffect());
    }
}
