package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawDiscardAndConniveEffect;

@CardRegistration(set = "MSC", collectorNumber = "616")
public class AtlanteanSkirmisher extends Card {

    public AtlanteanSkirmisher() {
        addEffect(EffectSlot.ON_ATTACK, new DrawDiscardAndConniveEffect());
    }
}
