package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M21", collectorNumber = "132")
public class BonePitBrute extends Card {

    public BonePitBrute() {
        // When this creature enters, target creature gets +4/+0 until end of turn.
        target(TargetFilters.creature())
                .addTrigger(Triggers.enters(ObjectRelation.THIS), BoostEffect.target(4, 0));
    }
}
