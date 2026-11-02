package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.VentureIntoDungeonEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "AFR", collectorNumber = "128")
public class YuanTiFangBlade extends Card {

    public YuanTiFangBlade() {
        addTrigger(Triggers.dealsCombatDamageToPlayer(), new VentureIntoDungeonEffect());
    }
}
