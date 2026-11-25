package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveAllCountersAsCostEffect;

import java.util.List;

@CardRegistration(set = "ULG", collectorNumber = "85")
public class MoltenHydra extends Card {

    public MoltenHydra() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{R}{R}",
                List.of(PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE)),
                "{1}{R}{R}: Put a +1/+1 counter on Molten Hydra."
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new RemoveAllCountersAsCostEffect(CounterType.PLUS_ONE_PLUS_ONE),
                        DealDamageEffect.toAnyTarget(new XValue())
                ),
                "{T}, Remove all +1/+1 counters from Molten Hydra: It deals damage to any target equal to the number of +1/+1 counters removed this way."
        ));
    }
}
