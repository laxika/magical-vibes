package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.AttacksAlone;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledByDefendingPlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MSC", collectorNumber = "609")
public class StrategicIntervention extends Card {

    public StrategicIntervention() {
        // Whenever a creature you control attacks alone, it gets +1/+1 until end of turn.

        // Tap up to one target creature defending player controls.
        var tapPermanents = new TapPermanentsEffect(Subjects.target());
        target(new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentControlledByDefendingPlayerPredicate())),
                "Target must be a creature defending player controls"), 0, 1)
                .bindEffect(tapPermanents);
        addTrigger(Triggers.creatureYouControlAttacks(), TriggerOptions.DEFAULT.withEventCondition(new AttacksAlone()),
                BoostEffect.of(Subjects.triggering(), 1, 1),
                tapPermanents);
    }
}
