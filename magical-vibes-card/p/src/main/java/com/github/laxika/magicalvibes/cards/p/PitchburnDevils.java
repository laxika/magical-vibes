package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ISD", collectorNumber = "156")
@CardRegistration(set = "M14", collectorNumber = "149")
@CardRegistration(set = "M21", collectorNumber = "156")
public class PitchburnDevils extends Card {

    public PitchburnDevils() {
        addTrigger(Triggers.dies(ObjectRelation.THIS), new DealDamageToAnyTargetEffect(3));
    }
}
