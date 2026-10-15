package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MaroGoneNutsEffect;

@CardRegistration(set = "MB1", collectorNumber = "81")
public class MarosGoneNuts extends Card {

    public MarosGoneNuts() {
        addEffect(EffectSlot.STATIC, new MaroGoneNutsEffect());
    }
}
