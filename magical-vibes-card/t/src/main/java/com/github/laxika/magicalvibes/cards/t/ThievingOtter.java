package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "IKO", collectorNumber = "69")
public class ThievingOtter extends Card {

    public ThievingOtter() {
        addTrigger(Triggers.dealsDamageToOpponent(), new DrawCardEffect());
    }
}
