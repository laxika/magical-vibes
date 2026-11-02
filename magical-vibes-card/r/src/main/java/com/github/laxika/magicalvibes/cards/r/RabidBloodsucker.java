package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ORI", collectorNumber = "113")
public class RabidBloodsucker extends Card {

    public RabidBloodsucker() {
        // When this creature enters, each player loses 2 life.
        addTrigger(Triggers.enters(ObjectRelation.THIS), new LoseLifeEffect(2, LoseLifeRecipient.EACH_PLAYER));
    }
}
