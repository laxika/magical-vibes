package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaValueParity;
import com.github.laxika.magicalvibes.model.condition.SourceCounterCountParity;
import com.github.laxika.magicalvibes.model.effect.CantAttackOrBlockUnlessEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DFT", collectorNumber = "221")
public class SabSunenLuxaEmbodied extends Card {

    public SabSunenLuxaEmbodied() {
        addStaticEffect(new CantAttackOrBlockUnlessEffect(
                new SourceCounterCountParity(ManaValueParity.EVEN),
                "it has an even number of counters on it"
        ));

        addTrigger(Triggers.precombatMainPhase(ActivePlayer.YOU), SequenceEffect.of(
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE),
                new ConditionalEffect(
                        new SourceCounterCountParity(ManaValueParity.ODD),
                        new DrawCardEffect(2))));
    }
}
