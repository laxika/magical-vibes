package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantTapDrawToRandomLandInLibraryEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YEOE", collectorNumber = "22")
public class AmbassadorOfEvendo extends Card {

    public AmbassadorOfEvendo() {
        addTrigger(Triggers.enters(Triggers.LAND, ObjectRelation.YOU_CONTROL),
                new PerpetuallyGrantTapDrawToRandomLandInLibraryEffect());
    }
}
