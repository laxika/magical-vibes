package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SPM", collectorNumber = "72")
@CardRegistration(set = "OM1", collectorNumber = "50")
@CardRegistration(set = "MSC", collectorNumber = "801")
public class VenomizedCat extends Card {

    public VenomizedCat() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new MillEffect(2, MillRecipient.CONTROLLER));
    }
}
