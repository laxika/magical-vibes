package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MMQ", collectorNumber = "261")
public class Pangosaur extends Card {

    public Pangosaur() {
        // Whenever a player plays a land, return this creature to its owner's hand.
        addTrigger(Triggers.youPlayLand(), ReturnToHandEffect.self());
        addTrigger(Triggers.opponentPlaysLand(), ReturnToHandEffect.self());
    }
}
