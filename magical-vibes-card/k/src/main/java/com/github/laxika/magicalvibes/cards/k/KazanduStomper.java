package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ReturnUpToNControlledPermanentsToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ZNR", collectorNumber = "191")
public class KazanduStomper extends Card {

    public KazanduStomper() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new ReturnUpToNControlledPermanentsToHandEffect(2, new PermanentIsLandPredicate(), "land"));
    }
}
