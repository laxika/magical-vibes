package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.UntapEnteringPermanentEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WWK", collectorNumber = "121")
@CardRegistration(set = "SLD", collectorNumber = "167")
@CardRegistration(set = "LCC", collectorNumber = "103")
public class AmuletOfVigor extends Card {

    public AmuletOfVigor() {
        addTrigger(Triggers.enters(null, ObjectRelation.ANY), new UntapEnteringPermanentEffect());
    }
}
