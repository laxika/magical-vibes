package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "LRW", collectorNumber = "185")
public class MudbuttonTorchrunner extends Card {

    public MudbuttonTorchrunner() {
        addEffect(EffectSlot.ON_DEATH, new DealDamageToAnyTargetEffect(3));
    }
}
