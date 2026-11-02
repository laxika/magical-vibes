package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PlaneswalkEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.StateTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentCounterCountAtLeastPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "OPC2", collectorNumber = "10")
@CardRegistration(set = "OPCA", collectorNumber = "13")
public class Aretopolis extends Card {

    public Aretopolis() {
        addTrigger(Triggers.planeswalkToThis(), SequenceEffect.of(
                new PutCountersOnSelfEffect(CounterType.SCROLL),
                new GainLifeEffect(new CountersOnSource(CounterType.SCROLL))));
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), SequenceEffect.of(
                new PutCountersOnSelfEffect(CounterType.SCROLL),
                new GainLifeEffect(new CountersOnSource(CounterType.SCROLL))));
        addTrigger(Triggers.chaosEnsues(), SequenceEffect.of(
                new PutCountersOnSelfEffect(CounterType.SCROLL),
                new DrawCardEffect(new CountersOnSource(CounterType.SCROLL))));
        addTrigger(Triggers.state(),
                new StateTriggerEffect(new PermanentCounterCountAtLeastPredicate(CounterType.SCROLL, 10),
                        List.of(new PlaneswalkEffect()), "Aretopolis's scroll counter ability"));
    }
}
