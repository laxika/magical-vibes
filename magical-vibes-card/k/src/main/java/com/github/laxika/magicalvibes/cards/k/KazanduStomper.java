package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.ReturnChosenPermanentsToHandEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ZNR", collectorNumber = "191")
public class KazanduStomper extends Card {

    public KazanduStomper() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                ReturnChosenPermanentsToHandEffect.upTo(2, Subjects.landsYouControl(), "land"));
    }
}
