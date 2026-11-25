package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.effect.RemoveCountersEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MRD", collectorNumber = "249")
@CardRegistration(set = "C13", collectorNumber = "261")
@CardRegistration(set = "C21", collectorNumber = "268")
@CardRegistration(set = "ARC", collectorNumber = "117")
public class SunDroplet extends Card {

    public SunDroplet() {
        addTrigger(Triggers.youAreDealtDamage(),
                PutCountersEffect.onSelf(CounterType.CHARGE, new EventValue()));
        addTrigger(Triggers.upkeep(ActivePlayer.ANY),
                new MayEffect(
                        SequenceEffect.of(
                                RemoveCountersEffect.fromSelf(CounterType.CHARGE, 1),
                                new ConditionalEffect(HappenedThisWay.removedCounters(), new GainLifeEffect(1))),
                        "Remove a charge counter from Sun Droplet to gain 1 life?"));
    }
}
