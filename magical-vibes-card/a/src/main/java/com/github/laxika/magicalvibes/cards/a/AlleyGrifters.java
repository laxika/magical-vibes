package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MMQ", collectorNumber = "115")
public class AlleyGrifters extends Card {

    public AlleyGrifters() {
        // Whenever this creature becomes blocked, defending player discards a card.
        addTrigger(Triggers.becomesBlocked(),
                new DiscardEffect(1, DiscardRecipient.DEFENDING_PLAYER));
    }
}
