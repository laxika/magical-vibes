package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

@CardRegistration(set = "RNA", collectorNumber = "216")
public class FootlightFiend extends Card {

    public FootlightFiend() {
        addEffect(EffectSlot.ON_DEATH, new DealDamageToAnyTargetEffect(1));
    }
}
