package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventDamageAndAddMinusCountersEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "MBS", collectorNumber = "85")
public class PhyrexianHydra extends Card {

    public PhyrexianHydra() {
        addEffect(EffectSlot.STATIC, new PreventDamageAndAddMinusCountersEffect());
    }
}
