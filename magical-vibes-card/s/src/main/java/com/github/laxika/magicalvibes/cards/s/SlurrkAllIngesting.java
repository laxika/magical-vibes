package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MOC", collectorNumber = "312")
public class SlurrkAllIngesting extends Card {

    private static final PermanentAllOfPredicate CONTROLLED_CREATURES_WITH_COUNTERS =
            new PermanentAllOfPredicate(List.of(
                    new PermanentIsCreaturePredicate(),
                    new PermanentHasCountersPredicate(CounterType.PLUS_ONE_PLUS_ONE)
            ));

    private static final PutCounterOnEachControlledPermanentEffect GROW_COUNTERED_CREATURES =
            new PutCounterOnEachControlledPermanentEffect(
                    CounterType.PLUS_ONE_PLUS_ONE, new Fixed(1), CONTROLLED_CREATURES_WITH_COUNTERS);


    public SlurrkAllIngesting() {
        addStaticEffect(new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new Fixed(5)));
        // "If it had a +1/+1 counter on it" is about whichever creature died, this one included: one ability with two
        // trigger conditions and the same filter, not a "this or another" relation, whose source matches whatever it was.
        addTriggeredAbility(new TriggeredAbility(List.of(
                        new TriggerCondition(Triggers.dies(
                                new PermanentHasCountersPredicate(CounterType.PLUS_ONE_PLUS_ONE), ObjectRelation.THIS)),
                        new TriggerCondition(Triggers.creatureDies(
                                new PermanentHasCountersPredicate(CounterType.PLUS_ONE_PLUS_ONE),
                                ObjectRelation.ANOTHER_YOU_CONTROL))),
                TriggerOptions.DEFAULT,
                List.of(new EffectRegistration(GROW_COUNTERED_CREATURES))));
    }
}
