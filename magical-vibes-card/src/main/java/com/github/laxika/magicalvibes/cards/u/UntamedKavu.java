package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EnterWithPlusOnePlusOneCountersIfKickedEffect;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;

@CardRegistration(set = "DOM", collectorNumber = "186")
public class UntamedKavu extends Card {

    public UntamedKavu() {
        addEffect(EffectSlot.STATIC, new KickerEffect("{3}"));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new EnterWithPlusOnePlusOneCountersIfKickedEffect(3));
    }
}
