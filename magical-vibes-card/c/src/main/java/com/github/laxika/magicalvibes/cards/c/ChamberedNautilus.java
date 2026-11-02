package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MMQ", collectorNumber = "64")
public class ChamberedNautilus extends Card {

    public ChamberedNautilus() {
        addTrigger(Triggers.becomesBlocked(), new MayEffect(new DrawCardEffect(), "Draw a card?"));
    }
}
