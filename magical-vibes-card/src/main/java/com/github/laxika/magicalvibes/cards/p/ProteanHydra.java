package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DelayedPlusOnePlusOneCounterRegrowthEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithXPlusOnePlusOneCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageAndRemovePlusOnePlusOneCountersEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "M10", collectorNumber = "200")
@CardRegistration(set = "M11", collectorNumber = "194")
public class ProteanHydra extends Card {

    public ProteanHydra() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new EnterWithXPlusOnePlusOneCountersEffect());
        addEffect(EffectSlot.STATIC, new PreventDamageAndRemovePlusOnePlusOneCountersEffect());
        addEffect(EffectSlot.STATIC, new DelayedPlusOnePlusOneCounterRegrowthEffect());
    }
}
