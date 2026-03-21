package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EnterWithPlusOnePlusOneCountersIfKickedEffect;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;

@CardRegistration(set = "DOM", collectorNumber = "105")
public class StrongholdConfessor extends Card {

    public StrongholdConfessor() {
        addEffect(EffectSlot.STATIC, new KickerEffect("{3}"));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new EnterWithPlusOnePlusOneCountersIfKickedEffect(2));
    }
}
