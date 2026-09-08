package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.PowerBoostForCrewAndSaddleEffect;

@CardRegistration(set = "DFT", collectorNumber = "123")
public class DynamiteDiver extends Card {

    public DynamiteDiver() {
        addEffect(EffectSlot.STATIC, new PowerBoostForCrewAndSaddleEffect(2));
        addEffect(EffectSlot.ON_DEATH, new DealDamageToAnyTargetEffect(1));
    }
}
