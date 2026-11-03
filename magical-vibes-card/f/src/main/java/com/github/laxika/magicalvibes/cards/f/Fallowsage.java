package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LRW", collectorNumber = "63")
@CardRegistration(set = "MOC", collectorNumber = "223")
public class Fallowsage extends Card {

    public Fallowsage() {
        // Whenever this creature becomes tapped, you may draw a card.
        addTrigger(Triggers.becomesTapped(), new MayEffect(new DrawCardEffect(1), "Draw a card?"));
    }
}
