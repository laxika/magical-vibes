package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastDiscardedCardWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LCI", collectorNumber = "63")
@CardRegistration(set = "LCI", collectorNumber = "293")
public class MalcolmAlluringScoundrel extends Card {

    public MalcolmAlluringScoundrel() {
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                SequenceEffect.of(
                        PutCountersEffect.onSelf(CounterType.CHORUS),
                        new DrawCardEffect(1),
                        new DiscardEffect(1, Subjects.you()),
                        new ConditionalEffect(new SourceCounterThreshold(4, CounterType.CHORUS),
                                new MayCastDiscardedCardWithoutPayingManaCostEffect(
                                        Subjects.cardsThisWay(StepKind.DISCARD)))));
    }
}
