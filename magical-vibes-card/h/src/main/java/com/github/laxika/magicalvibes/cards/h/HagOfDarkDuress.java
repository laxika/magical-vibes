package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;

@CardRegistration(set = "HBG", collectorNumber = "H18")
public class HagOfDarkDuress extends Card {

    public HagOfDarkDuress() {
        // At the beginning of your upkeep, each opponent discards a card.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new DiscardEffect(1, DiscardRecipient.EACH_OPPONENT));
    }
}
