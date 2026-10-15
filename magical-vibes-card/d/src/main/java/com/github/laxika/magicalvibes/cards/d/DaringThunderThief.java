package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;

@CardRegistration(set = "OTJ", collectorNumber = "41")
public class DaringThunderThief extends Card {

    public DaringThunderThief() {
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());
    }
}
