package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;

@CardRegistration(set = "MRD", collectorNumber = "69")
public class NecrogenMists extends Card {

    public NecrogenMists() {
        // At the beginning of each player's upkeep, that player discards a card.
        addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED, new DiscardEffect(1, DiscardRecipient.TARGET_PLAYER));
    }
}
