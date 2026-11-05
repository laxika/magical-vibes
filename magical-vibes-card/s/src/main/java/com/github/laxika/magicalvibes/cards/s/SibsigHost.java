package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FRF", collectorNumber = "82")
public class SibsigHost extends Card {

    public SibsigHost() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new MillEffect(3, MillRecipient.CONTROLLER),
                new MillEffect(3, MillRecipient.EACH_OPPONENT));
    }
}
