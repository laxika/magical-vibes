package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CanBeBlockedByAtMostNCreaturesEffect;

@CardRegistration(set = "POR", collectorNumber = "161")
@CardRegistration(set = "M15", collectorNumber = "171")
@CardRegistration(set = "TMP", collectorNumber = "218")
public class ChargingRhino extends Card {

    public ChargingRhino() {
        addEffect(EffectSlot.STATIC, new CanBeBlockedByAtMostNCreaturesEffect(1));
    }
}
