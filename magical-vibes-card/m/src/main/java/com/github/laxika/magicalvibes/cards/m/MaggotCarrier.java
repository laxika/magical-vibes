package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "8ED", collectorNumber = "142")
@CardRegistration(set = "PLS", collectorNumber = "45")
public class MaggotCarrier extends Card {

    public MaggotCarrier() {
        // When this creature enters, each player loses 1 life.
        addTrigger(Triggers.enters(ObjectRelation.THIS), new LoseLifeEffect(1, LoseLifeRecipient.EACH_PLAYER));
    }
}
