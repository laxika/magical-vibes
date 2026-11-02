package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.NoMaximumHandSizeEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FDN", collectorNumber = "123")
@CardRegistration(set = "FDN", collectorNumber = "350")
@CardRegistration(set = "FDN", collectorNumber = "411")
@CardRegistration(set = "FDN", collectorNumber = "482")
public class NivMizzetVisionary extends Card {

    public NivMizzetVisionary() {
        addStaticEffect(new NoMaximumHandSizeEffect());
        addTrigger(Triggers.opponentDealtNoncombatDamage(),
                new DrawCardEffect(new EventValue()));
    }
}
