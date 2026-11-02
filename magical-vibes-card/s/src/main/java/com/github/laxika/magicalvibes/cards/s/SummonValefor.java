package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.EachOpponentChoosesGreatestManaValueCreatureToReturnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "FIC", collectorNumber = "42")
@CardRegistration(set = "FIC", collectorNumber = "197")
public class SummonValefor extends Card {

    public SummonValefor() {
        addTrigger(Triggers.chapter(1),
                new EachOpponentChoosesGreatestManaValueCreatureToReturnToHandEffect());

        for (int chapter : Set.of(2, 3, 4)) {
            addTrigger(Triggers.chapter(chapter), new TapPermanentsEffect(TapUntapScope.TARGET));
            addTrigger(Triggers.chapter(chapter), new PutCounterOnTargetPermanentEffect(CounterType.STUN));
            setSagaChapterTargetFilter(chapter, Set.of(TargetFilters.creature()));
        }
    }
}
