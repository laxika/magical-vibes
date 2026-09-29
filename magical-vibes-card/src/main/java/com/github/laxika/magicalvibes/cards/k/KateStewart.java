package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCounterSum;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;

@CardRegistration(set = "WHO", collectorNumber = "139")
public class KateStewart extends Card {

    public KateStewart() {
        // Whenever you put one or more time counters on a permanent you control, create a Soldier.
        addEffect(EffectSlot.ON_YOU_PUT_TIME_COUNTERS_ON_CONTROLLED_PERMANENT,
                CreateTokenEffect.whiteSoldier(1));

        // Whenever Kate Stewart attacks, you may pay {8}. If you do, attacking creatures get +X/+X
        // until end of turn, where X is the number of time counters among permanents you control.
        PermanentCounterSum timeCounters = new PermanentCounterSum(
                CounterType.TIME, null, CountScope.CONTROLLER);
        addEffect(EffectSlot.ON_ATTACK, new MayPayManaEffect("{8}",
                new BoostAllCreaturesEffect(timeCounters, timeCounters,
                        new PermanentIsAttackingPredicate()),
                "Pay {8} to boost attacking creatures?"));
    }
}
