package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.ManaSpentToCast;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAtLeastCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "MIC", collectorNumber = "27")
@CardRegistration(set = "MIC", collectorNumber = "65")
public class KurbisHarvestCelebrant extends Card {

    public KurbisHarvestCelebrant() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new ManaSpentToCast()));

        PermanentPredicate anotherCounteredCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentHasAtLeastCountersPredicate(CounterType.PLUS_ONE_PLUS_ONE, 1),
                new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate())
        ));
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new RemoveCounterFromSourceCost(1, CounterType.PLUS_ONE_PLUS_ONE),
                        PreventDamageEffect.allToTargetCreatures()
                ),
                "Remove a +1/+1 counter from Kurbis: Prevent all damage that would be dealt this turn "
                        + "to another target creature with a +1/+1 counter on it.",
                new PermanentPredicateTargetFilter(
                        anotherCounteredCreature,
                        "Target must be another creature with a +1/+1 counter on it"
                )
        ));
    }
}
