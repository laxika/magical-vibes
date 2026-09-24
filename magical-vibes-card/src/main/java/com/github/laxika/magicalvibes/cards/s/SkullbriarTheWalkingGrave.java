package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreserveCountersOnZoneChangeEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;

@CardRegistration(set = "2X2", collectorNumber = "277")
@CardRegistration(set = "CMD", collectorNumber = "227")
public class SkullbriarTheWalkingGrave extends Card {

    public SkullbriarTheWalkingGrave() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new PutCountersOnSourceEffect(1, 1, 1));
        addEffect(EffectSlot.STATIC, new PreserveCountersOnZoneChangeEffect());
    }
}
