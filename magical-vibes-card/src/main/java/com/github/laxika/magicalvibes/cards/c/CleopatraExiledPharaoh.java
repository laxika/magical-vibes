package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardForEachDyingSourceCounterEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "ACR", collectorNumber = "52")
@CardRegistration(set = "ACR", collectorNumber = "119")
public class CleopatraExiledPharaoh extends Card {

    public CleopatraExiledPharaoh() {
        PermanentPredicate otherLegendaryCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentHasSupertypePredicate(CardSupertype.LEGENDARY),
                new PermanentNotPredicate(new PermanentIsSourceCardPredicate())
        ));
        target(new PermanentPredicateTargetFilter(
                otherLegendaryCreature, "Target must be another legendary creature"), 0, 2)
                .addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                        new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE));

        PermanentPredicate legendaryCreatureWithCounters = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentHasSupertypePredicate(CardSupertype.LEGENDARY),
                new PermanentHasCountersPredicate(CounterType.ANY)
        ));
        addEffect(EffectSlot.ON_ANY_CREATURE_DIES, new TriggeringPermanentConditionalEffect(
                legendaryCreatureWithCounters,
                new DrawCardForEachDyingSourceCounterEffect(null, new LoseLifeEffect(2))));
    }
}
