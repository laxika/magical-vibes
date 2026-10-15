package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;

import java.util.List;

@CardRegistration(set = "GTC", collectorNumber = "91")
public class FiveAlarmFire extends Card {

    public FiveAlarmFire() {
        // Whenever a creature you control deals combat damage, put a blaze counter on this enchantment.
        addEffect(EffectSlot.ON_ALLY_CREATURE_DEALS_COMBAT_DAMAGE, new PutCountersOnSelfEffect(CounterType.BLAZE));

        // Remove five blaze counters from this enchantment: It deals 5 damage to any target.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{0}",
                List.of(
                        new RemoveCounterFromSourceCost(5, CounterType.BLAZE),
                        new DealDamageToAnyTargetEffect(5)
                ),
                "Remove five blaze counters from Five-Alarm Fire: It deals 5 damage to any target."
        ));
    }
}
