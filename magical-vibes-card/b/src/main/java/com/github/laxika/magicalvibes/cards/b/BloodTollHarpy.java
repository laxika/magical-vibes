package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "THS", collectorNumber = "79")
public class BloodTollHarpy extends Card {

    public BloodTollHarpy() {
        // When this creature enters, each player loses 1 life.
        addTrigger(Triggers.enters(ObjectRelation.THIS), new LoseLifeEffect(1, LoseLifeRecipient.EACH_PLAYER));
    }
}
