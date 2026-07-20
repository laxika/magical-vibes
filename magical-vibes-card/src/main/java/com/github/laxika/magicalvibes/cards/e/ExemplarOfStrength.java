package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceAndGainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "AKH", collectorNumber = "165")
public class ExemplarOfStrength extends Card {

    public ExemplarOfStrength() {
        // "When this creature enters, put three -1/-1 counters on target creature you control."
        target(new ControlledPermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature you control"
        )).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new PutCounterOnTargetPermanentEffect(CounterType.MINUS_ONE_MINUS_ONE, 3));

        // "Whenever this creature attacks, remove a -1/-1 counter from it. If you do, you gain 1 life."
        addEffect(EffectSlot.ON_ATTACK, new RemoveCounterFromSourceAndGainLifeEffect(CounterType.MINUS_ONE_MINUS_ONE, 1));
    }
}
