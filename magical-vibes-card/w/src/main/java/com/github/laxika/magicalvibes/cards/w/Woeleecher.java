package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.effect.RemoveCountersEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "SHM", collectorNumber = "27")
public class Woeleecher extends Card {

    public Woeleecher() {
        // {W}, {T}: Remove a -1/-1 counter from target creature. If you do, you gain 2 life.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{W}",
                List.of(
                        RemoveCountersEffect.fromTarget(CounterType.MINUS_ONE_MINUS_ONE, new PermanentIsCreaturePredicate()),
                        new ConditionalEffect(HappenedThisWay.removedCounters(), new GainLifeEffect(2))),
                "{W}, {T}: Remove a -1/-1 counter from target creature. If you do, you gain 2 life.",
                TargetFilters.creature()
        ));
    }
}
