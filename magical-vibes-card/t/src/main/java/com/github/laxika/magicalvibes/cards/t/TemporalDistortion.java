package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.effect.RemoveCountersEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.MatchingPermanentsDoesntUntapEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "INV", collectorNumber = "79")
public class TemporalDistortion extends Card {

    private static final PermanentAnyOfPredicate CREATURE_OR_LAND = new PermanentAnyOfPredicate(List.of(
            new PermanentIsCreaturePredicate(),
            new PermanentIsLandPredicate()));

    public TemporalDistortion() {
        var hourglassCounter = new PutCountersEffect(
                Subjects.triggering(), CounterType.HOURGLASS);
        addTrigger(List.of(
                        Triggers.permanentYouControlBecomesTapped().withFilter(CREATURE_OR_LAND),
                        Triggers.permanentOpponentControlsBecomesTapped().withFilter(CREATURE_OR_LAND)),
                hourglassCounter);

        addStaticEffect(new MatchingPermanentsDoesntUntapEffect(
                new PermanentHasCountersPredicate(CounterType.HOURGLASS)));

        // "Remove all hourglass counters from permanents that player controls": the upkeep's player, which the
        // step-trigger collection puts on the entry without targeting.
        addTrigger(Triggers.upkeep(ActivePlayer.ANY), RemoveCountersEffect.allFrom(
                Subjects.controlledBy(Subjects.targetPlayerDeclaredElsewhere(),
                        new PermanentHasCountersPredicate(CounterType.HOURGLASS)),
                CounterType.HOURGLASS));
    }
}
