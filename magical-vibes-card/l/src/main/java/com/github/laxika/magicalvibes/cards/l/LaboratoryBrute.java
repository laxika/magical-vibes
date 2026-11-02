package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "EMN", collectorNumber = "67")
@CardRegistration(set = "SIR", collectorNumber = "79")
public class LaboratoryBrute extends Card {

    public LaboratoryBrute() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new MillEffect(4, MillRecipient.CONTROLLER));
    }
}
