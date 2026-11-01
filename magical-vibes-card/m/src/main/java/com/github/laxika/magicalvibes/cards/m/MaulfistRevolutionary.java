package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AddAnotherCounterOfEachKindToTargetEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "AER", collectorNumber = "115")
@CardRegistration(set = "KLR", collectorNumber = "168")
public class MaulfistRevolutionary extends Card {

    public MaulfistRevolutionary() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new AddAnotherCounterOfEachKindToTargetEffect());
        addTrigger(Triggers.dies(ObjectRelation.THIS), new AddAnotherCounterOfEachKindToTargetEffect());
    }
}
