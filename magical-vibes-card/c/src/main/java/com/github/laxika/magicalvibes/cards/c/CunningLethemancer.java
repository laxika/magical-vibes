package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ALA", collectorNumber = "69")
public class CunningLethemancer extends Card {

    public CunningLethemancer() {
        // At the beginning of your upkeep, each player discards a card (APNAP order).
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                new DiscardEffect(1, DiscardRecipient.EACH_PLAYER));
    }
}
