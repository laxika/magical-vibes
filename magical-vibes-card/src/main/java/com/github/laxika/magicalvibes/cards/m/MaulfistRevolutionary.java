package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AddAnotherCounterOfEachKindToTargetEffect;

@CardRegistration(set = "AER", collectorNumber = "115")
public class MaulfistRevolutionary extends Card {

    public MaulfistRevolutionary() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new AddAnotherCounterOfEachKindToTargetEffect());
        addEffect(EffectSlot.ON_DEATH, new AddAnotherCounterOfEachKindToTargetEffect());
    }
}
