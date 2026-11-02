package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardForYouAndDamagedPlayerEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MSC", collectorNumber = "504")
public class BlackWidowIntelExpert extends Card {

    public BlackWidowIntelExpert() {
        addTrigger(Triggers.dealsCombatDamageToPlayer(), new DrawCardForYouAndDamagedPlayerEffect(2));
    }
}
