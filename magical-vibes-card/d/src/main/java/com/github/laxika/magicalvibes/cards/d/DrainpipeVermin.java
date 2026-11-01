package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "RTR", collectorNumber = "66")
public class DrainpipeVermin extends Card {

    public DrainpipeVermin() {
        // When this creature dies, you may pay {B}. If you do, target player discards a card.
        addTrigger(Triggers.dies(ObjectRelation.THIS), new MayPayManaEffect("{B}",
                new DiscardEffect(1, DiscardRecipient.TARGET_PLAYER),
                "Pay {B} to make target player discard a card?"));
    }
}
