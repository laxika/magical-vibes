package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;

@CardRegistration(set = "ALA", collectorNumber = "69")
public class CunningLethemancer extends Card {

    public CunningLethemancer() {
        // At the beginning of your upkeep, each player discards a card (APNAP order).
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new DiscardEffect(1, DiscardRecipient.EACH_PLAYER));
    }
}
