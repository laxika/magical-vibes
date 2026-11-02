package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M11", collectorNumber = "103")
public class LilianasCaress extends Card {

    public LilianasCaress() {
        // Whenever an opponent discards a card, that player loses 2 life.
        addTrigger(Triggers.opponentDiscardsCard(), new LoseLifeEffect(2, LoseLifeRecipient.TRIGGERING_PLAYER));
    }
}
