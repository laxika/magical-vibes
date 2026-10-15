package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;

@CardRegistration(set = "IKO", collectorNumber = "3")
public class MysteriousEgg extends Card {

    public MysteriousEgg() {
        addEffect(EffectSlot.ON_SELF_MUTATES, new PutCountersOnSourceEffect(1, 1, 1));
    }
}
