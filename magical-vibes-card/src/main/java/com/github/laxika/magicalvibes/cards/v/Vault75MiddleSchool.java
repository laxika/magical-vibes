package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastPredicate;

import java.util.List;

/**
 * Vault 75: Middle School — {2}{W}{W} Enchantment — Saga
 *
 * (As this Saga enters and after your draw step, add a lore counter. Sacrifice after III.)
 * I — Exile all creatures with power 4 or greater.
 * II, III — Put a +1/+1 counter on each creature you control.
 */
@CardRegistration(set = "PIP", collectorNumber = "27")
@CardRegistration(set = "PIP", collectorNumber = "555")
public class Vault75MiddleSchool extends Card {

    public Vault75MiddleSchool() {
        addEffect(EffectSlot.SAGA_CHAPTER_I, new ExileAllPermanentsEffect(new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentPowerAtLeastPredicate(4)
        ))));

        addEffect(EffectSlot.SAGA_CHAPTER_II,
                new PutCounterOnEachControlledPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1,
                        new PermanentIsCreaturePredicate()));
        addEffect(EffectSlot.SAGA_CHAPTER_III,
                new PutCounterOnEachControlledPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1,
                        new PermanentIsCreaturePredicate()));
    }
}
