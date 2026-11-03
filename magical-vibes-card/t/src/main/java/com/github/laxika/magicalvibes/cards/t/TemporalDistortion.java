package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.EachPermanentScope;
import com.github.laxika.magicalvibes.model.effect.MatchingPermanentsDoesntUntapEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnReferencedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentReference;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromEachMatchingPermanentEffect;
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
        var hourglassCounter = new PutCounterOnReferencedPermanentEffect(
                PermanentReference.TRIGGERING, CounterType.HOURGLASS);
        addTrigger(List.of(
                        Triggers.permanentYouControlBecomesTapped().withFilter(CREATURE_OR_LAND),
                        Triggers.permanentOpponentControlsBecomesTapped().withFilter(CREATURE_OR_LAND)),
                hourglassCounter);

        addStaticEffect(new MatchingPermanentsDoesntUntapEffect(
                new PermanentHasCountersPredicate(CounterType.HOURGLASS)));

        addTrigger(Triggers.upkeep(ActivePlayer.ANY), new RemoveCounterFromEachMatchingPermanentEffect(
                CounterType.HOURGLASS,
                Integer.MAX_VALUE,
                new PermanentHasCountersPredicate(CounterType.HOURGLASS),
                EachPermanentScope.TARGET_PLAYER));
    }
}
