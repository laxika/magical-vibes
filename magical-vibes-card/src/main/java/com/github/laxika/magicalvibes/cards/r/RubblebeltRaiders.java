package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.EachPermanentScope;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachMatchingPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;

import java.util.List;

@CardRegistration(set = "GTC", collectorNumber = "224")
public class RubblebeltRaiders extends Card {

    public RubblebeltRaiders() {
        // Whenever this creature attacks, put a +1/+1 counter on it for each attacking creature you control.
        // The counters land only on the source itself; the count includes this creature.
        addEffect(EffectSlot.ON_ATTACK, new PutCounterOnEachMatchingPermanentEffect(
                CounterType.PLUS_ONE_PLUS_ONE,
                new PermanentCount(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentIsAttackingPredicate())),
                        CountScope.CONTROLLER),
                new PermanentIsSourceCardPredicate(),
                EachPermanentScope.ALL_PLAYERS));
    }
}
