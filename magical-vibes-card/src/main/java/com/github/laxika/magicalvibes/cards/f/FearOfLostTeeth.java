package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "DSK", collectorNumber = "97")
public class FearOfLostTeeth extends Card {

    public FearOfLostTeeth() {
        addEffect(EffectSlot.ON_DEATH, SequenceEffect.of(
                new DealDamageToAnyTargetEffect(1),
                new GainLifeEffect(1)));
    }
}
