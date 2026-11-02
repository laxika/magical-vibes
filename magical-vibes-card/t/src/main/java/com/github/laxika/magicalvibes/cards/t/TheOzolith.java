package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MoveAllCountersFromSourceToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutLeavingCreatureCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "IKO", collectorNumber = "237")
@CardRegistration(set = "SLD", collectorNumber = "1741")
@CardRegistration(set = "LTC", collectorNumber = "351")
@CardRegistration(set = "LTC", collectorNumber = "381")
public class TheOzolith extends Card {

    public TheOzolith() {
        addTrigger(Triggers.leavesBattlefield(
                        new PermanentAllOfPredicate(List.of(
                                Triggers.CREATURE,
                                new PermanentHasCountersPredicate(CounterType.ANY))),
                        ObjectRelation.ANOTHER_YOU_CONTROL),
                new PutLeavingCreatureCountersOnSourceEffect());

        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(), "Target must be a creature"))
                .addTrigger(Triggers.beginningOfCombat(ActivePlayer.YOU),
                        new ConditionalEffect(
                                new SourceCounterThreshold(1, CounterType.ANY),
                                new MayEffect(
                                        new MoveAllCountersFromSourceToTargetCreatureEffect(),
                                        "Move all counters from The Ozolith onto target creature?")));
    }
}
