package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileTopUntilNonlandMayCastByDiscardEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BLB", collectorNumber = "219")
public class TheInfamousCruelclaw extends Card {

    public TheInfamousCruelclaw() {
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new ExileTopUntilNonlandMayCastByDiscardEffect());
    }
}
