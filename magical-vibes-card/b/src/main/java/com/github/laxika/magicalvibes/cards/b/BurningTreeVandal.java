package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardAndDrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.RiotEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "RNA", collectorNumber = "94")
@CardRegistration(set = "RVR", collectorNumber = "105")
public class BurningTreeVandal extends Card {

    public BurningTreeVandal() {
        addStaticEffect(new RiotEffect());

        // Whenever this creature attacks, you may discard a card. If you do, draw a card.
        addTrigger(Triggers.attacks(), new MayEffect(
                new DiscardAndDrawCardEffect(), "Discard a card to draw a card?"
        ));
    }
}
