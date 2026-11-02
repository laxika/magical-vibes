package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ARB", collectorNumber = "64")
public class VithianRenegades extends Card {

    public VithianRenegades() {
        // When this creature enters, destroy target artifact.
        target(TargetFilters.artifact()).addTrigger(Triggers.enters(ObjectRelation.THIS),
                new DestroyTargetPermanentEffect());
    }
}
