package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscoverEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;

import java.util.List;

@CardRegistration(set = "EOC", collectorNumber = "11")
@CardRegistration(set = "EOC", collectorNumber = "31")
public class LongRangeSensor extends Card {

    public LongRangeSensor() {
        // Whenever you attack a player, put a charge counter on this artifact.
        addEffect(EffectSlot.ON_ALLY_CREATURES_ATTACK_PLAYER,
                new PutCountersOnSelfEffect(CounterType.CHARGE));

        // {1}, Remove two charge counters from this artifact: Discover 4. Activate only as a sorcery.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(
                        new RemoveCounterFromSourceCost(2, CounterType.CHARGE),
                        new DiscoverEffect(4)
                ),
                "{1}, Remove two charge counters from this artifact: Discover 4. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
