package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FDN", collectorNumber = "170")
@CardRegistration(set = "GRN", collectorNumber = "64")
@CardRegistration(set = "RVR", collectorNumber = "69")
public class BurglarRat extends Card {

    public BurglarRat() {
        // When this creature enters, each opponent discards a card.
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new DiscardEffect(1, DiscardRecipient.EACH_OPPONENT));
    }
}
