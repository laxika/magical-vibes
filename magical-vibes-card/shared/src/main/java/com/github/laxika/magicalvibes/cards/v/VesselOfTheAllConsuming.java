package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.LoseGameIfSourceDealtDamageToPlayerThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

public class VesselOfTheAllConsuming extends Card {

    public VesselOfTheAllConsuming() {
        addTrigger(Triggers.dealsDamage(), new PutCountersOnSourceEffect(1, 1, 1));
        addTrigger(Triggers.dealsDamageToPlayer(),
                new LoseGameIfSourceDealtDamageToPlayerThisTurnEffect(10));
    }
}
