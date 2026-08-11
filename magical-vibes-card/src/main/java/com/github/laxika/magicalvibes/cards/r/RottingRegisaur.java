package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;

@CardRegistration(set = "M20", collectorNumber = "111")
public class RottingRegisaur extends Card {

    public RottingRegisaur() {
        // At the beginning of your upkeep, discard a card.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new DiscardEffect(1, DiscardRecipient.CONTROLLER));
    }
}
