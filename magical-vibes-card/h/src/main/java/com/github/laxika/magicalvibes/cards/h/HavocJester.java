package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M21", collectorNumber = "149")
@CardRegistration(set = "CMM", collectorNumber = "230")
public class HavocJester extends Card {

    public HavocJester() {
        addTrigger(Triggers.sacrificed(null, ObjectRelation.YOU_CONTROL), new DealDamageToAnyTargetEffect(1));
    }
}
