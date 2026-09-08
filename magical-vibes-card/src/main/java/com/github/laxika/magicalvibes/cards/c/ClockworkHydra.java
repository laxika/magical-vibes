package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceThenEffect;

import java.util.List;

@CardRegistration(set = "TSP", collectorNumber = "253")
public class ClockworkHydra extends Card {

    public ClockworkHydra() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new Fixed(4)));

        var attackOrBlockEffect = new RemoveCounterFromSourceThenEffect(
                CounterType.PLUS_ONE_PLUS_ONE, new DealDamageToAnyTargetEffect(1));
        addEffect(EffectSlot.ON_ATTACK, attackOrBlockEffect);
        addEffect(EffectSlot.ON_BLOCK, attackOrBlockEffect);

        addActivatedAbility(new ActivatedAbility(true, null,
                List.of(new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE)),
                "{T}: Put a +1/+1 counter on Clockwork Hydra."));
    }
}
