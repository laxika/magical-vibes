package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "SOM", collectorNumber = "192")
public class PerilousMyr extends Card {

    public PerilousMyr() {
        addEffect(EffectSlot.ON_DEATH, new DealDamageToAnyTargetEffect(2));
    }
}
