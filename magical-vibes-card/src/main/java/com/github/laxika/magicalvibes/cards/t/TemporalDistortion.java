package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachPermanentScope;
import com.github.laxika.magicalvibes.model.effect.MatchingPermanentsDoesntUntapEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnReferencedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentReference;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromEachMatchingPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;

@CardRegistration(set = "INV", collectorNumber = "79")
public class TemporalDistortion extends Card {

    private static final PermanentAnyOfPredicate CREATURE_OR_LAND = new PermanentAnyOfPredicate(List.of(
            new PermanentIsCreaturePredicate(),
            new PermanentIsLandPredicate()));

    public TemporalDistortion() {
        var hourglassCounter = new PutCounterOnReferencedPermanentEffect(
                PermanentReference.TRIGGERING, CounterType.HOURGLASS);
        addEffect(EffectSlot.ON_ALLY_PERMANENT_BECOMES_TAPPED,
                new TriggeringPermanentConditionalEffect(CREATURE_OR_LAND, hourglassCounter));
        addEffect(EffectSlot.ON_OPPONENT_PERMANENT_BECOMES_TAPPED,
                new TriggeringPermanentConditionalEffect(CREATURE_OR_LAND, hourglassCounter));

        addEffect(EffectSlot.STATIC, new MatchingPermanentsDoesntUntapEffect(
                new PermanentHasCountersPredicate(CounterType.HOURGLASS)));

        addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED, new RemoveCounterFromEachMatchingPermanentEffect(
                CounterType.HOURGLASS,
                Integer.MAX_VALUE,
                new PermanentHasCountersPredicate(CounterType.HOURGLASS),
                EachPermanentScope.TARGET_PLAYER));
    }
}
