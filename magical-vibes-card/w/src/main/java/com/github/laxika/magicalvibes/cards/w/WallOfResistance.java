package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.condition.SelfWasDealtDamageThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MIR", collectorNumber = "46")
public class WallOfResistance extends Card {

    public WallOfResistance() {
        // At the beginning of each end step, if this creature was dealt damage this turn,
        // put a +0/+1 counter on it.
        addTrigger(Triggers.endStep(ActivePlayer.ANY), new ConditionalEffect(
                new SelfWasDealtDamageThisTurn(),
                new PutCountersOnSelfEffect(CounterType.PLUS_ZERO_PLUS_ONE)));
    }
}
