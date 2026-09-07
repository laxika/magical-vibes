package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.OpponentLostLifeThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "FDN", collectorNumber = "185")
@CardRegistration(set = "MID", collectorNumber = "123")
public class StromkirkBloodthief extends Card {

    public StromkirkBloodthief() {
        // At the beginning of your end step, if an opponent lost life this turn, put a +1/+1
        // counter on target Vampire you control.
        target(new ControlledPermanentPredicateTargetFilter(
                new PermanentHasSubtypePredicate(CardSubtype.VAMPIRE),
                "Target must be a Vampire you control"
        )).addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED, new ConditionalEffect(
                new OpponentLostLifeThisTurn(1),
                new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE)
        ));
    }
}
