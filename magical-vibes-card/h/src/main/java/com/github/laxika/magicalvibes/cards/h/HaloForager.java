package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PayXManaCastTargetInstantOrSorceryFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MOM", collectorNumber = "227")
@CardRegistration(set = "MOM", collectorNumber = "385")
@CardRegistration(set = "WOC", collectorNumber = "139")
public class HaloForager extends Card {

    public HaloForager() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new PayXManaCastTargetInstantOrSorceryFromGraveyardEffect());
    }
}
