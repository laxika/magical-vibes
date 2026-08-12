package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveXCountersFromSourceCost;

import java.util.List;

@CardRegistration(set = "DST", collectorNumber = "150")
public class TalonOfPain extends Card {

    public TalonOfPain() {
        addEffect(EffectSlot.ON_ALLY_SOURCE_DEALS_DAMAGE_TO_OPPONENT,
                new PutCountersOnSelfEffect(CounterType.CHARGE, true));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{X}",
                List.of(
                        new RemoveXCountersFromSourceCost(CounterType.CHARGE),
                        new DealDamageToAnyTargetEffect(new XValue())
                ),
                "{X}, {T}, Remove X charge counters from this artifact: It deals X damage to any target."
        ));
    }
}
