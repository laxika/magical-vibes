package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveUpToCountersFromAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "ECC", collectorNumber = "8")
@CardRegistration(set = "ECC", collectorNumber = "28")
public class EventidesShadow extends Card {

    public EventidesShadow() {
        addSpellEffect(SequenceEffect.of(
                RemoveUpToCountersFromAllPermanentsEffect.anyNumber(CounterType.ANY, Subjects.allPermanents()),
                new DrawCardEffect(new AmountThisWay(StepKind.REMOVE_COUNTERS)),
                new LoseLifeEffect(new AmountThisWay(StepKind.REMOVE_COUNTERS), Subjects.you())));
    }
}
