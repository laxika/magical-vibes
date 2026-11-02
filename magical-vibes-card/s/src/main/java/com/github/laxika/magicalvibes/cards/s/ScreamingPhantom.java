package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LCI", collectorNumber = "118")
public class ScreamingPhantom extends Card {

    public ScreamingPhantom() {
        // Whenever this creature attacks, mill a card.
        addTrigger(Triggers.attacks(), new MillEffect(1, MillRecipient.CONTROLLER));
    }
}
