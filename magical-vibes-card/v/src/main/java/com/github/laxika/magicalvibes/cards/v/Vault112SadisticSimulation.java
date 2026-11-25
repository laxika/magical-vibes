package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.SagaChapterTargetGroup;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerPaysAnyManaEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerPaysAnyManaEffect.Resource;
import com.github.laxika.magicalvibes.model.effect.EnergyCountersEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsAndMayCastSpellsEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "PIP", collectorNumber = "123")
@CardRegistration(set = "PIP", collectorNumber = "651")
public class Vault112SadisticSimulation extends Card {

    public Vault112SadisticSimulation() {
        addTrigger(Triggers.chapter(1),
                new TapPermanentsEffect(Subjects.target()),
                new PutCountersEffect(CounterType.STUN, 1),
                new EnergyCountersEffect(2));
        setSagaChapterTargetGroups(1,
                List.of(new SagaChapterTargetGroup(TargetFilters.creature(), 0, 1)));

        addTrigger(Triggers.chapter(2),
                new TapPermanentsEffect(Subjects.target()),
                new PutCountersEffect(CounterType.STUN, 1),
                new EnergyCountersEffect(2));
        setSagaChapterTargetGroups(2,
                List.of(new SagaChapterTargetGroup(TargetFilters.creature(), 0, 1)));

        addTrigger(Triggers.chapter(3), SequenceEffect.of(
                EachPlayerPaysAnyManaEffect.byYou(Resource.ENERGY),
                new ConditionalEffect(new HappenedThisWay(StepKind.PAY_ENERGY), SequenceEffect.of(
                        new ShuffleLibraryEffect(Subjects.you()),
                        ExileTopCardsAndMayCastSpellsEffect.controllerMayPlayUpTo(AmountThisWay.energyPaid(), 1)))));
    }
}
