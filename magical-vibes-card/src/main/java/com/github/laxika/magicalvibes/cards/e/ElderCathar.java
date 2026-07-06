package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.condition.TargetPermanentMatches;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "ISD", collectorNumber = "12")
public class ElderCathar extends Card {

    public ElderCathar() {
        // When Elder Cathar dies, put a +1/+1 counter on target creature you control.
        // If that creature is a Human, put two +1/+1 counters on it instead.
        target(new ControlledPermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature you control"
        )).addEffect(EffectSlot.ON_DEATH, new ConditionalReplacementEffect(new TargetPermanentMatches(new PermanentHasSubtypePredicate(CardSubtype.HUMAN)), new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1), new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 2)));
    }
}
