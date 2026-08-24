package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.DestroyEachTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "WAR", collectorNumber = "89")
public class TheElderspell extends Card {

    public TheElderspell() {
        target(new PermanentPredicateTargetFilter(
                new PermanentIsPlaneswalkerPredicate(),
                "Target must be a planeswalker"
        ), 0, 99).addEffect(EffectSlot.SPELL, new DestroyEachTargetPermanentEffect());

        addEffect(EffectSlot.SPELL, new PutCounterOnTargetPermanentEffect(
                CounterType.LOYALTY,
                new Scaled(new EventValue(), 2),
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsPlaneswalkerPredicate(),
                        new PermanentControlledBySourceControllerPredicate()
                )),
                null,
                false,
                null
        ));
    }
}
