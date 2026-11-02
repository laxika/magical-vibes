package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "IKO", collectorNumber = "66")
@CardRegistration(set = "HBG", collectorNumber = "912")
public class SeaDasherOctopus extends Card {

    public SeaDasherOctopus() {
        addTrigger(Triggers.dealsCombatDamageToPlayer(), new DrawCardEffect());
    }
}
