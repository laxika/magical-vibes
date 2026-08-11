package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "ECL", collectorNumber = "103")
public class GnarlbarkElm extends Card {

    public GnarlbarkElm() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.MINUS_ONE_MINUS_ONE, new Fixed(2)));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{B}",
                List.of(
                        new RemoveCounterFromSourceCost(2, CounterType.MINUS_ONE_MINUS_ONE),
                        new BoostTargetCreatureEffect(-2, -2)
                ),
                "{2}{B}, Remove two counters from this creature: Target creature gets -2/-2 until end of turn. "
                        + "Activate only as a sorcery.",
                TargetFilters.creature(),
                null,
                null,
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
