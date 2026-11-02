package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ConjureCardsAttachedToOpposingCreaturesEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YMKM", collectorNumber = "11")
public class PerforatorCrocodile extends Card {

    public PerforatorCrocodile() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new ConjureCardsAttachedToOpposingCreaturesEffect("RTR", "78"));
        addCycling("{1}{B}");
    }
}
