package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.CounterRemovalSubject;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterAndGainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "SHM", collectorNumber = "249")
public class Chainbreaker extends Card {

    public Chainbreaker() {
        // This creature enters with two -1/-1 counters on it.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.MINUS_ONE_MINUS_ONE, new Fixed(2)));

        // {3}, {T}: Remove a -1/-1 counter from target creature.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}",
                List.of(new RemoveCounterAndGainLifeEffect(CounterType.MINUS_ONE_MINUS_ONE, 0, CounterRemovalSubject.TARGET)),
                "{3}, {T}: Remove a -1/-1 counter from target creature.",
                TargetFilters.creature()
        ));
    }
}
