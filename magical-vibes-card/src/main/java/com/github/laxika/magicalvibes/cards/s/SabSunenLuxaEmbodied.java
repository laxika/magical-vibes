package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaValueParity;
import com.github.laxika.magicalvibes.model.condition.SourceCounterCountParity;
import com.github.laxika.magicalvibes.model.effect.CantAttackOrBlockUnlessEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "DFT", collectorNumber = "221")
public class SabSunenLuxaEmbodied extends Card {

    public SabSunenLuxaEmbodied() {
        addEffect(EffectSlot.STATIC, new CantAttackOrBlockUnlessEffect(
                new SourceCounterCountParity(ManaValueParity.EVEN),
                "it has an even number of counters on it"
        ));

        addEffect(EffectSlot.PRECOMBAT_MAIN_TRIGGERED, SequenceEffect.of(
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE),
                ConditionalEffect.unless(
                        new SourceCounterCountParity(ManaValueParity.ODD),
                        new DrawCardEffect(2))));
    }
}
