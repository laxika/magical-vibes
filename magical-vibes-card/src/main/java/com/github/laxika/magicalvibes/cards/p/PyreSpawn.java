package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

@CardRegistration(set = "VOW", collectorNumber = "173")
public class PyreSpawn extends Card {

    public PyreSpawn() {
        addEffect(EffectSlot.ON_DEATH, new DealDamageToAnyTargetEffect(3));
    }
}
