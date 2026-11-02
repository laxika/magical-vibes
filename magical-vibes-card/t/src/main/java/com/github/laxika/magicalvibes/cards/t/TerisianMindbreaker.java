package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MillHalfDefendingPlayerEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BRO", collectorNumber = "83")
public class TerisianMindbreaker extends Card {

    public TerisianMindbreaker() {
        addTrigger(Triggers.attacks(), new MillHalfDefendingPlayerEffect());
        addUnearth("{1}{U}{U}{U}");
    }
}
