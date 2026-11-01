package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LRW", collectorNumber = "109")
public class ExiledBoggart extends Card {

    public ExiledBoggart() {
        // When Exiled Boggart dies, discard a card.
        addTrigger(Triggers.dies(ObjectRelation.THIS), new DiscardEffect(1, DiscardRecipient.CONTROLLER));
    }
}
