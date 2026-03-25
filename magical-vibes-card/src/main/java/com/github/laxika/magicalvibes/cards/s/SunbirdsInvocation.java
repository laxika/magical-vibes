package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SunbirdsInvocationTriggerEffect;

@CardRegistration(set = "XLN", collectorNumber = "165")
public class SunbirdsInvocation extends Card {

    public SunbirdsInvocation() {
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SunbirdsInvocationTriggerEffect());
    }
}
