package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardAndDrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LCI", collectorNumber = "172")
public class VolatileWanderglyph extends Card {

    public VolatileWanderglyph() {
        addTrigger(Triggers.becomesTapped(),
                new MayEffect(new DiscardAndDrawCardEffect(), "Discard a card to draw a card?"));
    }
}
