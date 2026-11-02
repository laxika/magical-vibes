package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.LifeGainedThisTurn;
import com.github.laxika.magicalvibes.model.condition.GainedLifeThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DistributeCountersAmongTargetsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MUL", collectorNumber = "50")
@CardRegistration(set = "MUL", collectorNumber = "115")
@CardRegistration(set = "MUL", collectorNumber = "180")
public class LathielTheBounteousDawn extends Card {

    public LathielTheBounteousDawn() {
        PermanentPredicate otherCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate())));
        DistributeCountersAmongTargetsEffect distribution =
                DistributeCountersAmongTargetsEffect.chosenUpToAmongAnyNumberOfTargetCreatures(
                        CounterType.PLUS_ONE_PLUS_ONE,
                        new LifeGainedThisTurn(CountScope.CONTROLLER),
                        otherCreature);

        targetUpTo(new LifeGainedThisTurn(CountScope.CONTROLLER),
                new PermanentPredicateTargetFilter(otherCreature, "Target must be another creature"), Integer.MAX_VALUE)
                .addTrigger(Triggers.endStep(ActivePlayer.ANY),
                        new ConditionalEffect(new GainedLifeThisTurn(), distribution));
    }
}
