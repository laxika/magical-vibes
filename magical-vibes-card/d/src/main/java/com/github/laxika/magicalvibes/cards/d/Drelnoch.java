package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "CSP", collectorNumber = "32")
public class Drelnoch extends Card {

    public Drelnoch() {
        addTrigger(Triggers.becomesBlocked(), new MayEffect(new DrawCardEffect(2), "Draw two cards?"));
    }
}
