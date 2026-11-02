package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ReturnPermanentControlledByPlayerToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TSP", collectorNumber = "58")
@CardRegistration(set = "DDM", collectorNumber = "7")
@CardRegistration(set = "TSR", collectorNumber = "63")
public class DreamStalker extends Card {

    public DreamStalker() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new ReturnPermanentControlledByPlayerToHandEffect(
                new PermanentTruePredicate(),
                "permanent"
        ));
    }
}
