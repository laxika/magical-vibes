package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.MoveCounterFromTargetCreatureToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "FRF", collectorNumber = "9")
public class DaghatarTheAdamant extends Card {

    public DaghatarTheAdamant() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new Fixed(4)));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{B/G}{B/G}",
                List.of(MoveCounterFromTargetCreatureToTargetCreatureEffect.single(
                        CounterType.PLUS_ONE_PLUS_ONE)),
                "{1}{B/G}{B/G}: Move a +1/+1 counter from target creature onto a second target creature.",
                List.of(TargetFilters.creature(), TargetFilters.creature()),
                2,
                2
        ));
    }
}
