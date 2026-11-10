package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.SourceDealtDamageToTargetPlayerThisTurn;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerLosesGameEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

public class VesselOfTheAllConsuming extends Card {

    public VesselOfTheAllConsuming() {
        addTrigger(Triggers.dealsDamage(), new PutCountersOnSourceEffect(1, 1, 1));
        addTrigger(Triggers.dealsDamageToPlayer(),
                TriggerOptions.DEFAULT.withInterveningIf(new SourceDealtDamageToTargetPlayerThisTurn(10)),
                new TargetPlayerLosesGameEffect(null));
    }
}
