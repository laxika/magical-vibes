package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

@CardRegistration(set = "MSC", collectorNumber = "674")
public class VoidHelix extends Card {

    public VoidHelix() {
        addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(5));
        addEffect(EffectSlot.SPELL, new GainLifeEffect(5));
    }
}
