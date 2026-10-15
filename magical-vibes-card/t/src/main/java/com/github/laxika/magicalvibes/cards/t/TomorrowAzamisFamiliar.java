package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.TomorrowAzamisFamiliarDrawReplacementEffect;

@CardRegistration(set = "BOK", collectorNumber = "58")
public class TomorrowAzamisFamiliar extends Card {

    public TomorrowAzamisFamiliar() {
        addEffect(EffectSlot.STATIC, new TomorrowAzamisFamiliarDrawReplacementEffect());
    }
}
