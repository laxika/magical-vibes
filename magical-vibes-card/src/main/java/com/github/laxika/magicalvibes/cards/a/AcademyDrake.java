package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EnterWithPlusOnePlusOneCountersIfKickedEffect;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;

@CardRegistration(set = "DOM", collectorNumber = "40")
public class AcademyDrake extends Card {

    public AcademyDrake() {
        addEffect(EffectSlot.STATIC, new KickerEffect("{4}"));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new EnterWithPlusOnePlusOneCountersIfKickedEffect(2));
    }
}
