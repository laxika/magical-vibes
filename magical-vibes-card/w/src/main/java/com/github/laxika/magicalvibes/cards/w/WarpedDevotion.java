package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "8ED", collectorNumber = "172")
@CardRegistration(set = "PLS", collectorNumber = "57")
public class WarpedDevotion extends Card {

    public WarpedDevotion() {
        // Whenever a permanent is returned to a player's hand, that player discards a card.
        addTrigger(Triggers.putIntoHandFrom(Zone.BATTLEFIELD, null, ObjectRelation.ANY),
                new DiscardEffect(1, DiscardRecipient.TARGET_PLAYER, false));
    }
}
