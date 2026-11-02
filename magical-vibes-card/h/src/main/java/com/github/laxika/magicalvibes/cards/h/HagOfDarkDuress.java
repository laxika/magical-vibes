package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "HBG", collectorNumber = "H18")
public class HagOfDarkDuress extends Card {

    public HagOfDarkDuress() {
        // At the beginning of your upkeep, each opponent discards a card.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new DiscardEffect(1, DiscardRecipient.EACH_OPPONENT));
    }
}
