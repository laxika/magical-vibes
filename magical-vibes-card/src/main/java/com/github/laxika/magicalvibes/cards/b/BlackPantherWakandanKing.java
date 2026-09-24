package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MoveAllCountersOfTypeFromTargetPermanentToTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "SLD", collectorNumber = "1747")
public class BlackPantherWakandanKing extends Card {

    public BlackPantherWakandanKing() {
        // Whenever this creature or another creature you control enters, put a +1/+1 counter on
        // target land you control.
        target(TargetFilters.landYouControl()).addEffect(
                EffectSlot.ON_SELF_OR_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE));

        // {3}: Move all +1/+1 counters from target land you control onto target creature. If one
        // or more counters moved this way, gain that much life and draw a card.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}",
                List.of(
                        new MoveAllCountersOfTypeFromTargetPermanentToTargetPermanentEffect(
                                CounterType.PLUS_ONE_PLUS_ONE),
                        new GainLifeEffect(new EventValue()),
                        new DrawCardEffect(new EventValue())
                ),
                "{3}: Move all +1/+1 counters from target land you control onto target creature. If one or more +1/+1 counters are moved this way, you gain that much life and draw a card.",
                List.of(TargetFilters.landYouControl(), TargetFilters.creature()),
                2,
                2
        ).withAllowSharedTargets());
    }
}
