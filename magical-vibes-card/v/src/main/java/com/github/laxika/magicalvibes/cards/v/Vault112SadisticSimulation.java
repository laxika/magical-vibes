package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.SagaChapterTargetGroup;
import com.github.laxika.magicalvibes.model.effect.EnergyCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PayAnyAmountOfEnergyThenExileTopCardsAndMayPlayOneWithoutPayingEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "PIP", collectorNumber = "123")
@CardRegistration(set = "PIP", collectorNumber = "651")
public class Vault112SadisticSimulation extends Card {

    public Vault112SadisticSimulation() {
        addTrigger(Triggers.chapter(1), new TapPermanentsEffect(TapUntapScope.TARGET));
        addTrigger(Triggers.chapter(1),
                new PutCounterOnTargetPermanentEffect(CounterType.STUN, 1));
        addTrigger(Triggers.chapter(1), new EnergyCountersEffect(2));
        setSagaChapterTargetGroups(1,
                List.of(new SagaChapterTargetGroup(TargetFilters.creature(), 0, 1)));

        addTrigger(Triggers.chapter(2), new TapPermanentsEffect(TapUntapScope.TARGET));
        addTrigger(Triggers.chapter(2),
                new PutCounterOnTargetPermanentEffect(CounterType.STUN, 1));
        addTrigger(Triggers.chapter(2), new EnergyCountersEffect(2));
        setSagaChapterTargetGroups(2,
                List.of(new SagaChapterTargetGroup(TargetFilters.creature(), 0, 1)));

        addTrigger(Triggers.chapter(3),
                new PayAnyAmountOfEnergyThenExileTopCardsAndMayPlayOneWithoutPayingEffect());
    }
}
