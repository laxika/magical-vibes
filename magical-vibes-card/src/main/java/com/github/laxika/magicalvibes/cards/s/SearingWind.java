package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

@CardRegistration(set = "8ED", collectorNumber = "218")
public class SearingWind extends Card {

    public SearingWind() {
        addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(10));
    }
}
