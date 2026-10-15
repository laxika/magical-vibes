package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AdjustTimeCountersOnEachSuspendedCardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.EachPermanentScope;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachMatchingPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromEachMatchingPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;

import java.util.List;

@CardRegistration(set = "FUT", collectorNumber = "5")
public class DustOfMoments extends Card {

    public DustOfMoments() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Remove two time counters from each permanent and each suspended card",
                        List.of(
                                new RemoveCounterFromEachMatchingPermanentEffect(
                                        CounterType.TIME, 2,
                                        new PermanentHasCountersPredicate(CounterType.TIME),
                                        EachPermanentScope.ALL_PLAYERS),
                                new AdjustTimeCountersOnEachSuspendedCardEffect(false))),
                new ChooseOneEffect.ChooseOneOption(
                        "Put two time counters on each permanent with a time counter on it and each suspended card",
                        List.of(
                                new PutCounterOnEachMatchingPermanentEffect(
                                        CounterType.TIME, 2,
                                        new PermanentHasCountersPredicate(CounterType.TIME),
                                        EachPermanentScope.ALL_PLAYERS),
                                new AdjustTimeCountersOnEachSuspendedCardEffect(true)))
        )));
    }
}
