package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DST", collectorNumber = "123")
public class GethsGrimoire extends Card {

    public GethsGrimoire() {
        addTrigger(Triggers.opponentDiscardsCard(),
                new MayEffect(new DrawCardEffect(), "Draw a card?"));
    }
}
