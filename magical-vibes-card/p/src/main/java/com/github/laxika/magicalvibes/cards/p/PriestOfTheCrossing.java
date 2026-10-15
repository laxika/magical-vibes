package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.CreatureDeathsThisTurn;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "DRC", collectorNumber = "6")
@CardRegistration(set = "DRC", collectorNumber = "22")
public class PriestOfTheCrossing extends Card {

    public PriestOfTheCrossing() {
        // At the beginning of each end step, put a +1/+1 counter on each creature you control for
        // each creature that died under your control this turn.
        addEffect(EffectSlot.END_STEP_TRIGGERED, new PutCounterOnEachControlledPermanentEffect(
                CounterType.PLUS_ONE_PLUS_ONE,
                new CreatureDeathsThisTurn(CountScope.CONTROLLER),
                new PermanentIsCreaturePredicate()));
    }
}
