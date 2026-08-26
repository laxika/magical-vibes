package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.MultiTargetConstraint;
import com.github.laxika.magicalvibes.model.effect.EachPermanentScope;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachMatchingPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutTargetSpellOrCreatureOnTopOrBottomOfLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "MAT", collectorNumber = "8")
public class TolarianContempt extends Card {

    public TolarianContempt() {
        PermanentPredicate opponentCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new PutCounterOnEachMatchingPermanentEffect(
                        CounterType.REJECTION, 1, opponentCreature, EachPermanentScope.ALL_PLAYERS));

        PermanentPredicate opponentCreatureWithRejectionCounter = new PermanentAllOfPredicate(List.of(
                opponentCreature,
                new PermanentHasCountersPredicate(CounterType.REJECTION)));
        setMultiTargetConstraint(MultiTargetConstraint.AT_MOST_ONE_PER_CONTROLLER);
        target(new PermanentPredicateTargetFilter(
                        opponentCreatureWithRejectionCounter,
                        "Target must be a creature an opponent controls with a rejection counter"), 0, 99)
                .addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                        new PutTargetSpellOrCreatureOnTopOrBottomOfLibraryEffect());
    }
}
