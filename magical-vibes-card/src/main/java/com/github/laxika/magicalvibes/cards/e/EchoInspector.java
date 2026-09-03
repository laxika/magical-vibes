package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawDiscardAndConniveEffect;

@CardRegistration(set = "SNC", collectorNumber = "40")
public class EchoInspector extends Card {

    public EchoInspector() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DrawDiscardAndConniveEffect());
    }
}
