package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ReturnPermanentsOnCombatDamageToPlayerEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "10E", collectorNumber = "72")
@CardRegistration(set = "JUD", collectorNumber = "35")
public class CephalidConstable extends Card {

    public CephalidConstable() {
        addTrigger(Triggers.dealsCombatDamageToPlayer(), new ReturnPermanentsOnCombatDamageToPlayerEffect());
    }
}
