package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "POR", collectorNumber = "104")
public class NoxiousToad extends Card {

    public NoxiousToad() {
        // When this creature dies, each opponent discards a card.
        addTrigger(Triggers.dies(ObjectRelation.THIS), new DiscardEffect(1, DiscardRecipient.EACH_OPPONENT));
    }
}
