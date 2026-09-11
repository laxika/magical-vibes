package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastPredicate;

import java.util.List;

@CardRegistration(set = "HOB", collectorNumber = "130")
public class NastyLittleRabbit extends Card {

    public NastyLittleRabbit() {
        addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED, new ConditionalEffect(
                new ControlsPermanentCount(1, new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentPowerAtLeastPredicate(4)
                ))),
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE)
        ));
    }
}
