package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.MoveCounterFromSourceToEnteringCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;

@CardRegistration(set = "DIS", collectorNumber = "23")
public class CytoplastManipulator extends Card {

    public CytoplastManipulator() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new Fixed(2)));
        addEffect(EffectSlot.ON_ANY_OTHER_CREATURE_ENTERS_BATTLEFIELD,
                new MoveCounterFromSourceToEnteringCreatureEffect(CounterType.PLUS_ONE_PLUS_ONE));

        PermanentPredicate creatureWithCounter = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentHasCountersPredicate(CounterType.PLUS_ONE_PLUS_ONE)));
        addActivatedAbility(new ActivatedAbility(
                true,
                "{U}",
                List.of(GainControlOfTargetEffect.withTargetPredicate(
                        ControlDuration.WHILE_SOURCE_REMAINS, creatureWithCounter)),
                "{U}, {T}: Gain control of target creature with a +1/+1 counter on it for as long as this creature remains on the battlefield.",
                new PermanentPredicateTargetFilter(creatureWithCounter,
                        "Target must be a creature with a +1/+1 counter")));
    }
}
