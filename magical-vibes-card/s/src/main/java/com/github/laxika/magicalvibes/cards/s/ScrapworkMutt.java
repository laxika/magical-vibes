package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardAndDrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BRO", collectorNumber = "164")
public class ScrapworkMutt extends Card {

    public ScrapworkMutt() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new MayEffect(
                new DiscardAndDrawCardEffect(), "Discard a card to draw a card?"
        ));

        addUnearth("{1}{R}");
    }
}
