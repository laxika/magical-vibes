package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

@CardRegistration(set = "M20", collectorNumber = "83")
public class AgonizingSyphon extends Card {

    public AgonizingSyphon() {
        addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(3));
        addEffect(EffectSlot.SPELL, new GainLifeEffect(3));
    }
}
