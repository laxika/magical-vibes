package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.LifeGainedThisTurn;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;

import java.util.List;

@CardRegistration(set = "C17", collectorNumber = "40")
public class LiciaSanguineTribune extends Card {

    public LiciaSanguineTribune() {
        addEffect(EffectSlot.STATIC, new ReduceOwnCastCostEffect(
                new LifeGainedThisTurn(CountScope.CONTROLLER)));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new PayLifeCost(5),
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE, 3)),
                "Pay 5 life: Put three +1/+1 counters on Licia. Activate only during your turn and only once each turn.",
                null,
                null,
                1,
                ActivationTimingRestriction.ONLY_DURING_YOUR_TURN));
    }
}
