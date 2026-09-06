package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromControlledPermanentCost;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTappedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "EOE", collectorNumber = "40")
public class SunstarChaplain extends Card {

    public SunstarChaplain() {
        target(TargetFilters.creatureYouControl())
                .addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED, new ConditionalEffect(
                        new ControlsPermanentCount(2, new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentIsTappedPredicate()
                        ))),
                        new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE)
                ));

        PermanentAnyOfPredicate artifactOrCreature = new PermanentAnyOfPredicate(List.of(
                new PermanentIsArtifactPredicate(),
                new PermanentIsCreaturePredicate()
        ));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(
                        new RemoveCounterFromControlledPermanentCost(
                                CounterType.PLUS_ONE_PLUS_ONE,
                                new PermanentIsCreaturePredicate()),
                        new TapPermanentsEffect(TapUntapScope.TARGET)
                ),
                "{2}, Remove a +1/+1 counter from a creature you control: Tap target artifact or creature.",
                new PermanentPredicateTargetFilter(artifactOrCreature, "Target must be an artifact or creature")
        ));
    }
}
