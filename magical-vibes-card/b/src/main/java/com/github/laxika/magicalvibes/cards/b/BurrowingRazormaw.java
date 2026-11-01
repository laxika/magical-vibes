package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BRO", collectorNumber = "173")
public class BurrowingRazormaw extends Card {

    public BurrowingRazormaw() {
        addTrigger(Triggers.dies(ObjectRelation.THIS), new MillEffect(4, MillRecipient.CONTROLLER));
    }
}
