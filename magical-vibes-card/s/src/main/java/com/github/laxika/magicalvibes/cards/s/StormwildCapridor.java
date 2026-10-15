package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventDamageToOtherCreaturesAndAddPlusCountersEffect;

@CardRegistration(set = "IKO", collectorNumber = "34")
public class StormwildCapridor extends Card {

    public StormwildCapridor() {
        addEffect(EffectSlot.STATIC,
                PreventDamageToOtherCreaturesAndAddPlusCountersEffect.forSource(true));
    }
}
