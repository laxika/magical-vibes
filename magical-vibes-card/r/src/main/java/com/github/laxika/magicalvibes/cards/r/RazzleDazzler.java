package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "OTJ", collectorNumber = "63")
public class RazzleDazzler extends Card {

    public RazzleDazzler() {
        addTrigger(Triggers.youCastSpell().nthThisTurn(2),
                PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE),
                MakeCreatureUnblockableEffect.self());
    }
}
