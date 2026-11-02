package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "VIS", collectorNumber = "123")
@CardRegistration(set = "6ED", collectorNumber = "260")
public class UktabiOrangutan extends Card {

    public UktabiOrangutan() {
        target(TargetFilters.artifact()).addTrigger(Triggers.enters(ObjectRelation.THIS),
                new DestroyTargetPermanentEffect());
    }
}
