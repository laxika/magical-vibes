package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MOM", collectorNumber = "120")
public class NezumiInformant extends Card {

    public NezumiInformant() {
        // When this creature enters, each opponent discards a card.
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new DiscardEffect(1, DiscardRecipient.EACH_OPPONENT));
    }
}
