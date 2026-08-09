package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterOrSacrificeSelfEffect;

import java.util.List;

@CardRegistration(set = "NEM", collectorNumber = "65")
public class PhyrexianProwler extends Card {

    public PhyrexianProwler() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.FADE, new Fixed(3)));

        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new RemoveCounterOrSacrificeSelfEffect(CounterType.FADE));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new RemoveCounterFromSourceCost(1, CounterType.FADE),
                        new BoostSelfEffect(1, 1)
                ),
                "Remove a fade counter from this creature: This creature gets +1/+1 until end of turn."
        ));
    }
}
