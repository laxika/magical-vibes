package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExploreEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "XLN", collectorNumber = "189")
public class EmperorsVanguard extends Card {

    public EmperorsVanguard() {
        addTrigger(Triggers.dealsCombatDamageToPlayer(), new ExploreEffect());
    }
}
