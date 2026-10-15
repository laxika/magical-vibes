package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;

@CardRegistration(set = "RTR", collectorNumber = "66")
public class DrainpipeVermin extends Card {

    public DrainpipeVermin() {
        // When this creature dies, you may pay {B}. If you do, target player discards a card.
        addEffect(EffectSlot.ON_DEATH, new MayPayManaEffect("{B}",
                new DiscardEffect(1, DiscardRecipient.TARGET_PLAYER),
                "Pay {B} to make target player discard a card?"));
    }
}
