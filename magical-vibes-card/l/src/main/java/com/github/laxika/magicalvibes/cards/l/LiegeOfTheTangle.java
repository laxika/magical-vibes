package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PutAwakeningCountersOnTargetLandsEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SOM", collectorNumber = "123")
@CardRegistration(set = "2XM", collectorNumber = "174")
public class LiegeOfTheTangle extends Card {

    public LiegeOfTheTangle() {
        addTrigger(Triggers.dealsCombatDamageToPlayer(), new PutAwakeningCountersOnTargetLandsEffect());
    }
}
