package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachPermanentScope;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachMatchingPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "MBS", collectorNumber = "4")
public class ChokingFumes extends Card {

    public ChokingFumes() {
        // Put a -1/-1 counter on each attacking creature.
        addEffect(EffectSlot.SPELL, new PutCounterOnEachMatchingPermanentEffect(
                CounterType.MINUS_ONE_MINUS_ONE, 1,
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentIsAttackingPredicate())),
                EachPermanentScope.ALL_PLAYERS));
    }
}
