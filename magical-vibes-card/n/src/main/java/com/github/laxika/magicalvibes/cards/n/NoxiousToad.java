package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;

@CardRegistration(set = "POR", collectorNumber = "104")
public class NoxiousToad extends Card {

    public NoxiousToad() {
        // When this creature dies, each opponent discards a card.
        addEffect(EffectSlot.ON_DEATH, new DiscardEffect(1, DiscardRecipient.EACH_OPPONENT));
    }
}
