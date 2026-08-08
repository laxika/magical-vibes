package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

/**
 * Evolve itself is keyword-driven; only the "whenever this creature evolves" ability is wired here.
 */
@CardRegistration(set = "DGM", collectorNumber = "47")
public class RenegadeKrasis extends Card {

    public RenegadeKrasis() {
        // Whenever this creature evolves, put a +1/+1 counter on each other creature you control
        // with a +1/+1 counter on it.
        addEffect(EffectSlot.ON_SELF_EVOLVES, new PutCounterOnEachControlledPermanentEffect(
                CounterType.PLUS_ONE_PLUS_ONE,
                1,
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentHasCountersPredicate(CounterType.PLUS_ONE_PLUS_ONE),
                        new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate())
                ))
        ));
    }
}
