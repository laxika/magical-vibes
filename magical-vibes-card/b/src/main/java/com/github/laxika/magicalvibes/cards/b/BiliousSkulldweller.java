package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ToxicEffect;

@CardRegistration(set = "ONE", collectorNumber = "83")
public class BiliousSkulldweller extends Card {

    public BiliousSkulldweller() {
        addEffect(EffectSlot.STATIC, new ToxicEffect(1));
    }
}
