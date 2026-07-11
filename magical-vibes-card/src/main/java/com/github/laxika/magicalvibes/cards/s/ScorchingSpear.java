package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

@CardRegistration(set = "POR", collectorNumber = "148")
public class ScorchingSpear extends Card {

    public ScorchingSpear() {
        addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(1));
    }
}
