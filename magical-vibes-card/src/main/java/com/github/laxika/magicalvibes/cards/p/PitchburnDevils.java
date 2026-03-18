package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "ISD", collectorNumber = "156")
public class PitchburnDevils extends Card {

    public PitchburnDevils() {
        addEffect(EffectSlot.ON_DEATH, new DealDamageToAnyTargetEffect(3));
    }
}
