package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DKA", collectorNumber = "61")
public class FarbogBoneflinger extends Card {

    public FarbogBoneflinger() {
        // When this creature enters, target creature gets -2/-2 until end of turn.
        target(TargetFilters.creature()).addTrigger(Triggers.enters(ObjectRelation.THIS),
                new BoostTargetCreatureEffect(-2, -2));
    }
}
