package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedIfAttackingAloneEffect;
import com.github.laxika.magicalvibes.model.effect.VentureIntoDungeonEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "AFR", collectorNumber = "86")
public class YuanTiMalison extends Card {

    public YuanTiMalison() {
        addStaticEffect(new CantBeBlockedIfAttackingAloneEffect());
        addTrigger(Triggers.dealsCombatDamageToPlayer(), new VentureIntoDungeonEffect());
    }
}
