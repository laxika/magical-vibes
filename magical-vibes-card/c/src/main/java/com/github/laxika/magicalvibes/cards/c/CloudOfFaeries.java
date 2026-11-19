package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ULG", collectorNumber = "29")
@CardRegistration(set = "VMA", collectorNumber = "62")
@CardRegistration(set = "DMR", collectorNumber = "43")
@CardRegistration(set = "MOC", collectorNumber = "219")
@CardRegistration(set = "WOC", collectorNumber = "86")
public class CloudOfFaeries extends Card {

    public CloudOfFaeries() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new UntapPermanentsEffect(Subjects.controlledBy(Subjects.eachPlayer(),
                        new PermanentIsLandPredicate()), null, 2));
        addCycling("{2}");
    }
}
