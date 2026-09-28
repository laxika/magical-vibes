package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.SagaChapterTargetGroup;
import com.github.laxika.magicalvibes.model.effect.EnergyCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PayAnyAmountOfEnergyThenExileTopCardsAndMayPlayOneWithoutPayingEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "PIP", collectorNumber = "123")
@CardRegistration(set = "PIP", collectorNumber = "651")
public class Vault112SadisticSimulation extends Card {

    public Vault112SadisticSimulation() {
        addEffect(EffectSlot.SAGA_CHAPTER_I, new TapPermanentsEffect(TapUntapScope.TARGET));
        addEffect(EffectSlot.SAGA_CHAPTER_I,
                new PutCounterOnTargetPermanentEffect(CounterType.STUN, 1));
        addEffect(EffectSlot.SAGA_CHAPTER_I, new EnergyCountersEffect(2));
        setSagaChapterTargetGroups(EffectSlot.SAGA_CHAPTER_I,
                List.of(new SagaChapterTargetGroup(TargetFilters.creature(), 0, 1)));

        addEffect(EffectSlot.SAGA_CHAPTER_II, new TapPermanentsEffect(TapUntapScope.TARGET));
        addEffect(EffectSlot.SAGA_CHAPTER_II,
                new PutCounterOnTargetPermanentEffect(CounterType.STUN, 1));
        addEffect(EffectSlot.SAGA_CHAPTER_II, new EnergyCountersEffect(2));
        setSagaChapterTargetGroups(EffectSlot.SAGA_CHAPTER_II,
                List.of(new SagaChapterTargetGroup(TargetFilters.creature(), 0, 1)));

        addEffect(EffectSlot.SAGA_CHAPTER_III,
                new PayAnyAmountOfEnergyThenExileTopCardsAndMayPlayOneWithoutPayingEffect());
    }
}
