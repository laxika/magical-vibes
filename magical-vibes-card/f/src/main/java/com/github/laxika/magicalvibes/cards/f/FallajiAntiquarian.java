package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ConjureDuplicateOfTargetPermanentIntoGraveyardWithUnearthEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YBRO", collectorNumber = "8")
public class FallajiAntiquarian extends Card {

    public FallajiAntiquarian() {
        target(1, 1).addTrigger(Triggers.enters(ObjectRelation.THIS),
                new ConjureDuplicateOfTargetPermanentIntoGraveyardWithUnearthEffect());
    }
}
