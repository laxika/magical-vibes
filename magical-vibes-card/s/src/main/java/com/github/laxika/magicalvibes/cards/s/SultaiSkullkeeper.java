package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FRF", collectorNumber = "53")
@CardRegistration(set = "UMA", collectorNumber = "75")
public class SultaiSkullkeeper extends Card {

    public SultaiSkullkeeper() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new MillEffect(2, MillRecipient.CONTROLLER));
    }
}
