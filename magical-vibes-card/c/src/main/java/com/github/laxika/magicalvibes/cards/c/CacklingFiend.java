package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "USG", collectorNumber = "121")
@CardRegistration(set = "BRB", collectorNumber = "11")
@CardRegistration(set = "DMR", collectorNumber = "76")
public class CacklingFiend extends Card {

    public CacklingFiend() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new DiscardEffect(1, DiscardRecipient.EACH_OPPONENT));
    }
}
