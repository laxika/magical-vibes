package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "ECL", collectorNumber = "57")
public class LochMare extends Card {

    public LochMare() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.MINUS_ONE_MINUS_ONE, new Fixed(3)));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{U}",
                List.of(
                        new RemoveCounterFromSourceCost(1, CounterType.MINUS_ONE_MINUS_ONE),
                        new DrawCardEffect(1)
                ),
                "{1}{U}, Remove a counter from this creature: Draw a card."
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{U}",
                List.of(
                        new RemoveCounterFromSourceCost(2, CounterType.MINUS_ONE_MINUS_ONE),
                        new TapPermanentsEffect(TapUntapScope.TARGET),
                        new PutCounterOnTargetPermanentEffect(CounterType.STUN)
                ),
                "{2}{U}, Remove two counters from this creature: Tap target creature. Put a stun counter on it.",
                TargetFilters.creature()
        ));
    }
}
