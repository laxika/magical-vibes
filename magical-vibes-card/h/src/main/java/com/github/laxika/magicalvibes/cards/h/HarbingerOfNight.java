package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachPermanentScope;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachMatchingPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "MIR", collectorNumber = "128")
public class HarbingerOfNight extends Card {

    public HarbingerOfNight() {
        // At the beginning of your upkeep, put a -1/-1 counter on each creature.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new PutCounterOnEachMatchingPermanentEffect(
                CounterType.MINUS_ONE_MINUS_ONE, 1,
                new PermanentIsCreaturePredicate(),
                EachPermanentScope.ALL_PLAYERS));
    }
}
