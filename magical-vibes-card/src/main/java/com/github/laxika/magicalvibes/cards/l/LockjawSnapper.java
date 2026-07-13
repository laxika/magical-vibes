package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachPermanentScope;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachMatchingPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "SHM", collectorNumber = "255")
public class LockjawSnapper extends Card {

    public LockjawSnapper() {
        // When this creature dies, put a -1/-1 counter on each creature with a -1/-1 counter on it.
        addEffect(EffectSlot.ON_DEATH, new PutCounterOnEachMatchingPermanentEffect(
                CounterType.MINUS_ONE_MINUS_ONE, 1,
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentHasCountersPredicate(CounterType.MINUS_ONE_MINUS_ONE))),
                EachPermanentScope.ALL_PLAYERS));
    }
}
