package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "MH2", collectorNumber = "84")
public class FeastOfSanity extends Card {

    public FeastOfSanity() {
        addEffect(EffectSlot.ON_CONTROLLER_DISCARDS,
                SequenceEffect.of(new DealDamageToAnyTargetEffect(1), new GainLifeEffect(1)));
    }
}
