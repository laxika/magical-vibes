package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.DrawCardIfEventValueAtLeastEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MoveAllCountersFromTargetPermanentToTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "MAR", collectorNumber = "87")
public class BlackPantherWakandanKing extends Card {

    public BlackPantherWakandanKing() {
        target(TargetFilters.landYouControl()).addEffect(
                EffectSlot.ON_SELF_OR_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}",
                List.of(
                        new MoveAllCountersFromTargetPermanentToTargetPermanentEffect(
                                CounterType.PLUS_ONE_PLUS_ONE),
                        new GainLifeEffect(new EventValue()),
                        new DrawCardIfEventValueAtLeastEffect(1)),
                "{3}: Move all +1/+1 counters from target land you control onto target creature. "
                        + "If one or more +1/+1 counters are moved this way, you gain that much life and draw a card.",
                List.of(TargetFilters.landYouControl(), TargetFilters.creature()),
                2,
                2
        ).withAllowSharedTargets());
    }
}
