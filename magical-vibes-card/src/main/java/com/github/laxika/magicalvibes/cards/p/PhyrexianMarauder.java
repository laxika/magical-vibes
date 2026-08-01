package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.CantAttackUnlessPaysPerCounterEffect;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;

@CardRegistration(set = "VIS", collectorNumber = "151")
public class PhyrexianMarauder extends Card {

    public PhyrexianMarauder() {
        // This creature enters with X +1/+1 counters on it.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new XValue()));
        // This creature can't block.
        addEffect(EffectSlot.STATIC, new CantBlockEffect());
        // This creature can't attack unless you pay {1} for each +1/+1 counter on it.
        // Read at declare-attackers time by CombatAttackService via AttackCostEffect.
        addEffect(EffectSlot.STATIC,
                new CantAttackUnlessPaysPerCounterEffect(CounterType.PLUS_ONE_PLUS_ONE, 1));
    }
}
