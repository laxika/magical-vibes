package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CemeteryDesecratorEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "VOW", collectorNumber = "100")
@CardRegistration(set = "DBL", collectorNumber = "367")
public class CemeteryDesecrator extends Card {

    public CemeteryDesecrator() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new CemeteryDesecratorEffect());
        addTrigger(Triggers.dies(ObjectRelation.THIS), new CemeteryDesecratorEffect());
    }
}
