package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventDamageAndAddPlusCountersEffect;

@CardRegistration(set = "RAV", collectorNumber = "218")
public class Phytohydra extends Card {

    public Phytohydra() {
        addEffect(EffectSlot.STATIC, new PreventDamageAndAddPlusCountersEffect());
    }
}
