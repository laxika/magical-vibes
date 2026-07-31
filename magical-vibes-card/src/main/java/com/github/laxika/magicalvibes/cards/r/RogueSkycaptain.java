package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.OpponentGainsControlOfSourceCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PayPerCounterOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveAllCountersFromSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

import java.util.List;

@CardRegistration(set = "ALL", collectorNumber = "79")
public class RogueSkycaptain extends Card {

    public RogueSkycaptain() {
        // At the beginning of your upkeep, put a wage counter on this creature. You may pay {2}
        // for each wage counter on it. If you don't, remove all wage counters from this creature
        // and an opponent gains control of it. One sequence, so the new counter lands before the
        // payment is sized (two slot effects would be two separate stack entries).
        addEffect(EffectSlot.UPKEEP_TRIGGERED, SequenceEffect.of(
                new PutCountersOnSelfEffect(CounterType.WAGE),
                new PayPerCounterOrElseEffect(CounterType.WAGE, "{2}", List.of(
                        new RemoveAllCountersFromSelfEffect(CounterType.WAGE),
                        new OpponentGainsControlOfSourceCreatureEffect(ControlDuration.PERMANENT)
                ))
        ));
    }
}
